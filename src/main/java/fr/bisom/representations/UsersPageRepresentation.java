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
    List<UserRepresentation> users;

    /**
     * Total users count
     */
    int count;

    /**
     * For unserializing
     */
    protected UsersPageRepresentation() {
    }

    public UsersPageRepresentation(List<UserRepresentation> users, int count) {
        this.users = users;
        this.count = count;
    }

    public List<UserRepresentation> getUsers() {
        return users;
    }

    public void setUsers(List<UserRepresentation> users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
