package fr.bisom.params;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class UserCriteria {

    @QueryParam("groupId")
    List<String> groups;
    @QueryParam("roleId")
    List<String> roles;
    @QueryParam("search")
    String search;
    @QueryParam("lastName")
    String last;
    @QueryParam("firstName")
    String first;
    @QueryParam("email")
    String email;
    @QueryParam("username")
    String username;
    @QueryParam("enabled")
    Boolean enabled;
    @QueryParam("emailVerified")
    Boolean emailVerified;
    @QueryParam("first")
    Integer firstResult;
    @QueryParam("max")
    Integer maxResults;
    @QueryParam("briefRepresentation")
    Boolean briefRepresentation;
    @QueryParam("withoutGroupsOnly")
    Boolean withoutGroupsOnly;
    @Context
    UriInfo uriInfo;

    public UserCriteria() {
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Boolean getBriefRepresentation() {
        return briefRepresentation;
    }

    public void setBriefRepresentation(Boolean briefRepresentation) {
        this.briefRepresentation = briefRepresentation;
    }

    public Boolean getWithoutGroupsOnly() {
        return withoutGroupsOnly;
    }

    public void setWithoutGroupsOnly(Boolean withoutGroupsOnly) {
        this.withoutGroupsOnly = withoutGroupsOnly;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
