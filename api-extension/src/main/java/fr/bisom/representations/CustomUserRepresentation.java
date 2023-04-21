package fr.bisom.representations;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Map;
import java.util.Set;

public class CustomUserRepresentation extends UserRepresentation {

    private Map<String, Set<RoleRepresentation>> fullClientRoles;
    private Set<RoleRepresentation> fullRealmRoles;

    public Map<String, Set<RoleRepresentation>> getFullClientRoles() {
        return fullClientRoles;
    }

    public void setFullClientRoles(Map<String, Set<RoleRepresentation>> fullClientRoles) {
        this.fullClientRoles = fullClientRoles;
    }

    public Set<RoleRepresentation> getFullRealmRoles() {
        return fullRealmRoles;
    }

    public void setFullRealmRoles(Set<RoleRepresentation> fullRealmRoles) {
        this.fullRealmRoles = fullRealmRoles;
    }
}
