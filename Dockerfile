#
# Build keycloak extension
#
FROM maven:3.6.3-jdk-11-slim AS KeycloakExtension
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip=true

FROM jboss/keycloak:12.0.4
COPY --from=KeycloakExtension /home/app/target/keycloak-search-criteria-12.0.4.jar /opt/jboss/keycloak/standalone/deployments/

