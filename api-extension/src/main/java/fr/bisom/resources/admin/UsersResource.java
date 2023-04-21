package fr.bisom.resources.admin;

import fr.bisom.models.utils.ModelToCustomRepresentation;
import fr.bisom.params.UserCriteria;
import fr.bisom.queries.GetUsersByCriteriaQuery;
import fr.bisom.representations.CustomUserRepresentation;
import fr.bisom.representations.UsersPageRepresentation;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extended UsersResource
 */
public class UsersResource extends org.keycloak.services.resources.admin.UsersResource {

    private AdminPermissionEvaluator auth;
    private KeycloakSession kcSession;
    private List<ClientModel> clients;

    protected static final Logger logger = Logger.getLogger(UsersResource.class);

    public UsersResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(session, auth, adminEvent);
        this.auth = auth;
        this.kcSession = session;
        this.clients = kcSession.clients().getClientsStream(realm).collect(Collectors.toList());
    }

    /**
     * Get users by criteria
     * <p>
     * Returns a list of users, filtered according to query parameters.
     * Differs from the standard getUsers by being able to filter on groups or roles
     * Adding multiple groups or multiple roles returns a union of all users that fit those groups or roles
     * Adding multiple groups and multiple roles will give the intersection of users that belong both to the the specified groups and the specified roles
     * If either groups or roles are specified in the call, paging with "first" and/or "max" is impossible, and trying will return a 501.
     *
     * @param groups              A list of group Ids
     * @param roles               A list of realm role names
     * @param search              A special search field: when used, all other search fields are ignored. Can be something like id:abcd-efg-123
     *                            or a string which can be contained in the first+last name, email or username
     * @param last                A user's last name
     * @param first               A user's first name
     * @param email               A user's email
     * @param username            A user's username
     * @param enabled             User account status (enabled/disabled)
     * @param emailVerified       User email verification status
     * @param firstResult         Pagination offset
     * @param maxResults          Maximum results size (defaults to 100) - only taken into account if no group / role is defined
     * @param briefRepresentation Response format option
     * @param withoutGroupsOnly   Return users without group memberships
     * @return A list of users corresponding to the searched parameters, as well as the total count of users
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public UsersPageRepresentation getUsersByCriteria(@BeanParam UserCriteria criteria) {
        boolean withoutGroupsOnlyB = criteria.getWithoutGroupsOnly() != null && criteria.getWithoutGroupsOnly();
        GetUsersByCriteriaQuery qry = new GetUsersByCriteriaQuery(kcSession, auth);

        if (criteria.getSearch() != null && !criteria.getSearch().isEmpty()) {
            qry.addPredicateSearchGlobal(criteria.getSearch());
        } else {
            qry.addPredicateSearchFields(criteria.getLast(), criteria.getFirst(), criteria.getEmail(), criteria.getUsername());
        }

        qry.addPredicateBooleanSearchFields(criteria.getEnabled(), criteria.getEmailVerified());

        if (withoutGroupsOnlyB) {
            qry.addPredicateWithoutGroupsOnly();
        } else {
            qry.addPredicateForGroups(criteria.getGroups());
        }


        MultivaluedMap<String, String> queryParams = criteria.getUriInfo().getQueryParameters();
        List<String> fields = Arrays.stream(UserCriteria.class.getDeclaredFields())
                .map(f -> f.getAnnotation(QueryParam.class))
                .filter(Objects::nonNull)
                .map(QueryParam::value)
                .collect(Collectors.toList());
        Map<String, String> attributes = queryParams.entrySet().stream()
                .filter(param -> !fields.contains(param.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
        qry.addPredicateForAttributes(attributes);

        qry.addPredicateForRoles(criteria.getRoles());
        qry.applyPredicates();

        int count = qry.getTotalCount();
        List<UserModel> results = qry.execute(criteria.getFirstResult(), criteria.getMaxResults());
        return new UsersPageRepresentation(toUserRepresentation(realm, clients, auth.users(), criteria.getBriefRepresentation(), results), count);
    }

    /**
     * Converts a list of UserModels to a list of UserRepresentations
     *
     * @param realm
     * @param usersEvaluator
     * @param briefRepresentation
     * @param userModels
     * @return a list of UserRepresentations
     */
    private List<CustomUserRepresentation> toUserRepresentation(RealmModel realm, List<ClientModel> clients, UserPermissionEvaluator usersEvaluator, Boolean briefRepresentation, List<UserModel> userModels) {
        boolean briefRepresentationB = briefRepresentation != null && briefRepresentation;
        List<CustomUserRepresentation> results = new ArrayList<>();
        boolean canViewGlobal = usersEvaluator.canView();

        usersEvaluator.grantIfNoPermission(kcSession.getAttribute(UserModel.GROUPS) != null);

        for (UserModel user : userModels) {
            if (!canViewGlobal && !usersEvaluator.canView(user)) {
                continue;
            }
            CustomUserRepresentation userRep = briefRepresentationB
                    ? ModelToCustomRepresentation.toBriefCustomRepresentation(user)
                    : ModelToCustomRepresentation.toCustomRepresentation(kcSession, realm, clients, user);
            userRep.setAccess(usersEvaluator.getAccess(user));
            results.add(userRep);
        }
        return results;
    }
}