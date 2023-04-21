package fr.bisom.resources.admin;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.Path;

public class RealmAdminResource extends org.keycloak.services.resources.admin.RealmAdminResource {

    private final AdminEventBuilder adminEvent;

    public RealmAdminResource(AdminPermissionEvaluator auth, AdminEventBuilder adminEvent, KeycloakSession session) {
        super(session, auth, adminEvent);
        this.adminEvent = adminEvent;
    }

    /**
     * Base path for managing users in this realm.
     *
     * @return
     */
    @Path("users")
    @Override
    public UsersResource users() {
        UsersResource users = new UsersResource(session, auth, adminEvent);
        ResteasyProviderFactory.getInstance().injectProperties(users);
        return users;
    }

}
