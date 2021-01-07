package fr.bisom.resources.admin;

import fr.bisom.models.utils.ModelToCustomRepresentation;
import fr.bisom.queries.GetUsersByCriteriaQuery;
import fr.bisom.representations.CustomUserRepresentation;
import fr.bisom.representations.UsersPageRepresentation;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended UsersResource
 */
public class UsersResource extends org.keycloak.services.resources.admin.UsersResource {

    private AdminPermissionEvaluator auth;
    private KeycloakSession kcSession;
    private List<ClientModel> clients;

    public UsersResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(session.getContext().getRealm(), auth, adminEvent);
        this.auth = auth;
        this.kcSession = session;
        this.clients = kcSession.clients().getClients(realm);
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
     * @param groups     A list of group Ids
     * @param roles      A list of realm role names
     * @param search     A special search field: when used, all other search fields are ignored. Can be something like id:abcd-efg-123
     *                   or a string which can be contained in the first+last name, email or username
     * @param last       A user's last name
     * @param first      A user's first name
     * @param email      A user's email
     * @param username   A user's username
     * @param first      Pagination offset
     * @param maxResults Maximum results size (defaults to 100) - only taken into account if no group / role is defined
     * @return A list of users corresponding to the searched parameters, as well as the total count of users
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public UsersPageRepresentation getUsersByCriteria(@QueryParam("groupId") List<String> groups,
                                                      @QueryParam("roleId") List<String> roles,
                                                      @QueryParam("search") String search,
                                                      @QueryParam("lastName") String last,
                                                      @QueryParam("firstName") String first,
                                                      @QueryParam("email") String email,
                                                      @QueryParam("username") String username,
                                                      @QueryParam("enabled") Boolean enabled,
                                                      @QueryParam("emailVerified") Boolean emailVerified,
                                                      @QueryParam("first") Integer firstResult,
                                                      @QueryParam("max") Integer maxResults,
                                                      @QueryParam("briefRepresentation") Boolean briefRepresentation) {
        GetUsersByCriteriaQuery qry = new GetUsersByCriteriaQuery(kcSession, auth);

        if (search != null && !search.isEmpty()) {
            qry.addPredicateSearchGlobal(search);
        } else {
            qry.addPredicateSearchFields(last, first, email, username);
        }

        qry.addPredicateBooleanSearchFields(enabled, emailVerified);
        qry.addPredicateForGroups(groups);
        qry.addPredicateForRoles(roles);
        qry.applyPredicates();

        int count = qry.getTotalCount();
        List<UserModel> results = qry.execute(firstResult, maxResults);
        return new UsersPageRepresentation(toUserRepresentation(realm, clients, auth.users(), briefRepresentation, results), count);
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
