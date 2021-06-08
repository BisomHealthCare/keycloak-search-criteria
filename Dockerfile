#
# Build keycloak extension
#
FROM maven:3.6.3-jdk-11-slim AS KeycloakExtension
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip=true

FROM jboss/keycloak:13.0.1
COPY --from=KeycloakExtension /home/app/target/keycloak-search-criteria-13.0.1.jar /opt/jboss/keycloak/standalone/deployments/

