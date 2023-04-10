package fr.bisom;

import fr.bisom.resources.admin.AdminRoot;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * RealmResourceProviderFactory implementation to create the SPI provider
 */
public class UsersRealmResourceProviderFactory implements RealmResourceProviderFactory {

    protected static final Logger logger = Logger.getLogger(UsersRealmResourceProviderFactory.class);

    private static final String ID = "extended-api";

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        return new UsersRealmResourceProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        // Nothing to do
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public String getId() {
        return ID;
    }
}
