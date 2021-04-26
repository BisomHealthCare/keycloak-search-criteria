package fr.bisom.queries;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetUsersByCriteriaQuery {
    private static final String SEARCH_ID_PARAMETER = "id:"; // From org.keycloak.services.resources.admin.UsersResource

    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final EntityManager em;
    private CriteriaBuilder builder;
    private CriteriaQuery<UserEntity> userEntityQry;
    private Root<UserEntity> userEntityRoot;
    private List<Predicate> predicates = new ArrayList<>();

    /**
     * Creates an instance of FindUsersByCriteriaQuery: this object is used to build a FindUsersByCriteria request
     *
     * @param session
     * @param auth
     */
    public GetUsersByCriteriaQuery(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.builder = em.getCriteriaBuilder();

        UserPermissionEvaluator userPermissionEvaluator = auth.users();
        userPermissionEvaluator.requireView();
        userPermissionEvaluator.requireQuery();

        this.userEntityQry = builder.createQuery(UserEntity.class);
        this.userEntityRoot = userEntityQry.from(UserEntity.class);

        predicates.add(builder.equal(userEntityRoot.get("realmId"), session.getContext().getRealm().getId()));
    }

    /**
     * Add predicates to search users by first name, last name, email and username
     *
     * @param last     Searched last name
     * @param first    Searched first name
     * @param email    Searched email
     * @param username Searched username
     */
    public void addPredicateSearchFields(String last, String first, String email, String username) {
        int count = predicates.size();
        if (last != null && !last.isEmpty()) {
            addPredicateLike(userEntityRoot.get(UserModel.LAST_NAME), last);
        }
        if (first != null && !first.isEmpty()) {
            addPredicateLike(userEntityRoot.get(UserModel.FIRST_NAME), first);
        }
        if (email != null && !email.isEmpty()) {
            addPredicateLike(userEntityRoot.get(UserModel.EMAIL), email);
        }
        if (username != null && !username.isEmpty()) {
            addPredicateLike(userEntityRoot.get(UserModel.USERNAME), username);
        }
        boolean includeServiceAccount = predicates.size() != count;
        session.setAttribute(UserModel.INCLUDE_SERVICE_ACCOUNT, includeServiceAccount);
        if (!includeServiceAccount) {
            predicates.add(userEntityRoot.get("serviceAccountClientLink").isNull());
        }
        if (!auth.users().canView()) {
            Set<String> groupModels = auth.groups().getGroupsWithViewPermission();

            if (!groupModels.isEmpty()) {
                session.setAttribute(UserModel.GROUPS, groupModels);
            }
        }
    }

    /**
     * Add predicates to filter users by account status and email verification status
     *
     * @param enabled       User account status
     * @param emailVerified User email verification status
     */
    public void addPredicateBooleanSearchFields(Boolean enabled, Boolean emailVerified) {
        if (enabled != null) {
            predicates.add(builder.equal(userEntityRoot.get(UserModel.ENABLED), enabled));
        }
        if (emailVerified != null) {
            predicates.add(builder.equal(userEntityRoot.get("emailVerified"), emailVerified));
        }
    }

    /**
     * Add predicates to search users using a global criteria which can match part of either the username, the full name (first + last name) or the email
     *
     * @param search The searched value
     */
    public void addPredicateSearchGlobal(String search) {
        if (search.startsWith(SEARCH_ID_PARAMETER)) {
            String id = search.substring(SEARCH_ID_PARAMETER.length()).trim();
            predicates.add(builder.equal(userEntityRoot.get("id"), id));
            return;
        }
        predicates.add(builder.isNull(userEntityRoot.get("serviceAccountClientLink")));

        Expression<String> fullNameExpr = builder.concat(builder.concat(userEntityRoot.get("firstName"), " "), userEntityRoot.get("lastName"));
        predicates.add(builder.or(
                createPredicateLike(fullNameExpr, search),
                createPredicateLike(userEntityRoot.get("username"), search),
                createPredicateLike(userEntityRoot.get("email"), search)
        ));
    }

    /**
     * Add a filtering according to the given list of group identifiers
     *
     * @param groups
     */
    public void addPredicateForGroups(List<String> groups) {
        if (groups != null && !groups.isEmpty()) {
            this.auth.groups().requireView();
            predicates.add(userEntityRoot.get("id").in(createGroupsSubQuery(groups)));
        }
    }

    /**
     * Add a predicate to retrieve all user that are not members of a group
     */
    public void addPredicateWithoutGroupsOnly() {
        this.auth.groups().requireView();
        Subquery<UserGroupMembershipEntity> ugmSubquery = userEntityQry.subquery(UserGroupMembershipEntity.class);
        Root<UserGroupMembershipEntity> userGroupMembership = ugmSubquery.from(UserGroupMembershipEntity.class);
        predicates.add(builder.not(userEntityRoot.get("id").in(ugmSubquery.select(userGroupMembership.get("user").get("id")))));
    }

    /**
     * Add a filtering according to the given list of role identifiers
     *
     * @param roles
     */
    public void addPredicateForRoles(List<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            auth.roles().requireView(session.getContext().getRealm());
            predicates.add(userEntityRoot.get("id").in(createRolesSubQuery(roles)));
        }
    }

    private void addPredicateLike(Expression<String> expr, String value) {
        if (value != null && !value.isEmpty()) {
            predicates.add(createPredicateLike(expr, value));
        }
    }

    private Predicate createPredicateLike(Expression<String> expr, String value) {
        return builder.like(builder.lower(expr), "%" + value.toLowerCase() + "%");
    }

    private Subquery<?> createGroupsSubQuery(List<String> groups) {
        Subquery<UserGroupMembershipEntity> ugmSubquery = userEntityQry.subquery(UserGroupMembershipEntity.class);
        Root<UserGroupMembershipEntity> userGroupMembership = ugmSubquery.from(UserGroupMembershipEntity.class);
        return ugmSubquery
                .select(userGroupMembership.get("user").get("id"))
                .where(userGroupMembership.get("groupId").in(groups));
    }

    private Subquery<?> createRolesSubQuery(List<String> roles) {
        Subquery<UserRoleMappingEntity> urmSubquery = userEntityQry.subquery(UserRoleMappingEntity.class);
        Root<UserRoleMappingEntity> userRoleMembership = urmSubquery.from(UserRoleMappingEntity.class);
        return urmSubquery
                .select(userRoleMembership.get("user").get("id"))
                .where(userRoleMembership.get("roleId").in(roles));
    }

    /**
     * Predicates have been created: use them in the entity query
     */
    public void applyPredicates() {
        userEntityQry.where(predicates.toArray(new Predicate[predicates.size()])).orderBy(builder.asc(userEntityRoot.get(UserModel.USERNAME)));
        predicates.clear();
    }

    /**
     * Get the total number of matched rows
     *
     * @return number of matched rows
     */
    public int getTotalCount() {
        return em.createQuery(userEntityQry).getResultList().size();
    }

    /**
     * Get a view of the matched rows
     *
     * @param firstResult
     * @param maxResults
     * @return
     */
    public List<UserModel> execute(Integer firstResult, Integer maxResults) {
        TypedQuery<UserEntity> query = em.createQuery(userEntityQry);

        if (firstResult != null && firstResult >= 0) {
            query.setFirstResult(firstResult);
        }

        if (maxResults != null && maxResults >= 1) {
            query.setMaxResults(maxResults);
        }

        RealmModel realm = session.getContext().getRealm();
        UserProvider users = session.users();

        return query.getResultList().stream()
                .map(entity -> users.getUserById(entity.getId(), realm))
                .collect(Collectors.toList());
    }
}
