package fr.bisom.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.Cors;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminCorsPreflightService;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class AdminRoot extends org.keycloak.services.resources.admin.AdminRoot {

    protected static final Logger logger = Logger.getLogger(AdminRoot.class);

    public AdminRoot(KeycloakSession session) {
        this.session = session;
    }

    private HttpRequest getHttpRequest() {
        return this.session.getContext().getHttpRequest();
    }

    private HttpResponse getHttpResponse() {
        return this.session.getContext().getHttpResponse();
    }

    /**
     * Base Path to realm admin REST interface
     *
     * @return
     */
    @Path("realms")
    public Object getRealmsAdmin() {

        HttpRequest request = this.getHttpRequest();

        if (request.getHttpMethod().equals("OPTIONS")) {
            return new AdminCorsPreflightService(request);
        }

        AdminAuth auth = authenticateRealmAdminRequest(request.getHttpHeaders());
        if (auth == null) {
            throw new NotAuthorizedException("Can't get AdminAuth");
        }

        HttpResponse response = this.getHttpResponse();
        Cors.add(request).allowedOrigins(auth.getToken()).allowedMethods("GET", "PUT", "POST", "DELETE").exposedHeaders("Location").auth().build(response);

        org.keycloak.services.resources.admin.RealmsAdminResource adminResource = new RealmsAdminResource(auth, tokenManager, session);
        ResteasyProviderFactory.getInstance().injectProperties(adminResource);
        return adminResource;
    }

}
