package fr.bisom.models.utils;

import fr.bisom.representations.CustomUserRepresentation;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.storage.StorageId;

import java.util.*;
import java.util.stream.Collectors;

public class ModelToCustomRepresentation extends ModelToRepresentation {

    public static CustomUserRepresentation toCustomRepresentation(KeycloakSession session, RealmModel realm, List<ClientModel> clients, UserModel user) {
        CustomUserRepresentation rep = new CustomUserRepresentation();
        rep.setId(user.getId());
        String providerId = StorageId.resolveProviderId(user);
        rep.setOrigin(providerId);
        rep.setUsername(user.getUsername());
        rep.setCreatedTimestamp(user.getCreatedTimestamp());
        rep.setLastName(user.getLastName());
        rep.setFirstName(user.getFirstName());
        rep.setEmail(user.getEmail());
        rep.setEnabled(user.isEnabled());
        rep.setEmailVerified(user.isEmailVerified());
        rep.setTotp(session.userCredentialManager().isConfiguredFor(realm, user, "otp"));
        rep.setDisableableCredentialTypes(session.userCredentialManager().getDisableableCredentialTypes(realm, user));
        rep.setFederationLink(user.getFederationLink());
        rep.setNotBefore(session.users().getNotBeforeOfUser(realm, user));
        rep.setFullClientRoles(clients.stream().collect(Collectors.toMap(
                ClientModel::getClientId,
                client -> user.getClientRoleMappings(client).stream()
                        .map(ModelToRepresentation::toRepresentation)
                        .collect(Collectors.toSet()))
        ));
        rep.setFullRealmRoles(user.getRealmRoleMappings().stream()
                .map(ModelToRepresentation::toRepresentation)
                .collect(Collectors.toSet())
        );
        Set<String> requiredActions = user.getRequiredActions();
        List<String> reqActions = new ArrayList(requiredActions);
        rep.setRequiredActions(reqActions);
        Map<String, List<String>> attributes = user.getAttributes();
        Map<String, List<String>> copy = null;
        if (attributes != null) {
            copy = new HashMap(attributes);
            copy.remove("lastName");
            copy.remove("firstName");
            copy.remove("email");
            copy.remove("username");
        }

        if (attributes != null && !copy.isEmpty()) {
            Map<String, List<String>> attrs = new HashMap(copy);
            rep.setAttributes(attrs);
        }

        return rep;
    }

    public static CustomUserRepresentation toBriefCustomRepresentation(UserModel user) {
        CustomUserRepresentation rep = new CustomUserRepresentation();
        rep.setId(user.getId());
        rep.setUsername(user.getUsername());
        rep.setCreatedTimestamp(user.getCreatedTimestamp());
        rep.setLastName(user.getLastName());
        rep.setFirstName(user.getFirstName());
        rep.setEmail(user.getEmail());
        rep.setEnabled(user.isEnabled());
        rep.setEmailVerified(user.isEmailVerified());
        rep.setFederationLink(user.getFederationLink());
        return rep;
    }

}
