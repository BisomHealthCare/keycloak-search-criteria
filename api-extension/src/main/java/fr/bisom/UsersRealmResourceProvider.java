package fr.bisom;

import fr.bisom.resources.admin.AdminRoot;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * RealmResourceProvider implementation to register the new JAX-RS Resource (Custom API extension)
 */
public class UsersRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;


    public UsersRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new AdminRoot(session);
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
