# keycloak-search-criteria

This project is an extension of the Keycloak Admin REST API.
It brings an improved version of the existing `GET /{realm}/users` endpoint that allows filtering by :
 - Group and role membership.
 - User account status (enabled or not)
 - User email verification status

The user representation returned by this endpoint also includes realm and client roles for each user.

# Compatibility table 

This table lists the compatibility of this extension with keycloak releases. Other versions might be compatible but have not been tested.

| Extension version  | Compatible keycloak versions  |
|---|---|
| 12.0.4  | 12.0.4 |
| 11.0.3  | 11.0.3 |

# Installation

To deploy the extension, package this project as a JAR with the following command.
(Requires java 8 and maven 3.x)

```shell
mvn clean package
```
Then copy it to the `standalone/deployments/` directory of your keycloak instance,
as described in the [keycloak documentation](https://www.keycloak.org/docs/latest/server_development/#registering-provider-implementations).

# Usage

To use this endpoint you have to be authenticated as an Admin of the master realm as described in the [Keycloak documentation](https://www.keycloak.org/docs/latest/server_development/index.html#examples-using-curl).

Once you have retrieved an access token, you have to include it in the `Authorization` header of your request following the [The OAuth 2.0 Authorization Framework: Bearer Token pattern](https://tools.ietf.org/html/rfc6750)


#### Endpoint

```shell
GET /auth/realms/master/extended-api/realms/{realm}/users
```

#### Parameters

| Type  | Name  | Description  | Schema |
|---|---|---|---|
| Path  | `required` realm  | 	realm name (not id!)  |  string | 
| Query  | `optional` briefRepresentation | Brief or extended user representation response (defaults to false)  | boolean |
| Query  | `optional` email |   | string |
| Query  | `optional` first | Paging offset  | integer(int32) |
| Query  | `optional` firstName |   | string |
| Query  | `optional` lastName |   | string |
| Query  | `optional` max | Maximum results size (defaults to 100)  | integer(int32)  |
| Query  | `optional` search | A String contained in username, first or last name, or email  | string |
| Query  | `optional` username |   | string |
| Query  | `optional` roleId |   | string |
| Query  | `optional` groupId |  | string |
| Query  | `optional` enabled |  | boolean |
| Query  | `optional` emailVerified |  | boolean |
| Query  | `optional` withoutGroupsOnly | If true, returns only user's without group memberships (defaults to false) | boolean |

#### Responses

| HTTP Code  | Description  | Schema |
|---|---|---|
| 200   | 	success  |  < [UserRepresentation](https://www.keycloak.org/docs-api/5.0/rest-api/index.html#_userrepresentation) > array | 