package fr.bisom.representations;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * A paginated user list representation
 */
public class UsersPageRepresentation {

    /**
     * Users
     */
    List<CustomUserRepresentation> users;

    /**
     * Total users count
     */
    int count;

    /**
     * For unserializing
     */
    protected UsersPageRepresentation() {
    }

    public UsersPageRepresentation(List<CustomUserRepresentation> users, int count) {
        this.users = users;
        this.count = count;
    }

    public List<CustomUserRepresentation> getUsers() {
        return users;
    }

    public void setUsers(List<CustomUserRepresentation> users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
