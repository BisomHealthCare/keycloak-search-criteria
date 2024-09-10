#
# Build keycloak extension
#
FROM maven:3.8.3-jdk-11-slim AS KeycloakExtension
WORKDIR /workspace/app
COPY pom.xml iam/
COPY 2fa-email-authenticator/pom.xml iam/2fa-email-authenticator/
COPY api-extension/pom.xml iam/api-extension/
COPY magic-link-authenticator/pom.xml iam/magic-link-authenticator/

RUN mvn --batch-mode dependency:go-offline clean install -DskipTests -B -f iam

COPY 2fa-email-authenticator/src iam/2fa-email-authenticator/src
COPY api-extension/src iam/api-extension/src
COPY magic-link-authenticator/src iam/magic-link-authenticator/src

RUN mvn --batch-mode clean install -DskipTests -f iam

FROM bitnami/keycloak:25.0.5
COPY --from=KeycloakExtension /workspace/app/iam/2fa-email-authenticator/target/keycloak-2fa-email-authenticator-1.0.0.0-SNAPSHOT.jar /opt/bitnami/keycloak/providers/
COPY --from=KeycloakExtension /workspace/app/iam/api-extension/target/keycloak-search-criteria-25.0.5.jar /opt/bitnami/keycloak/providers/
COPY --from=KeycloakExtension /workspace/app/iam/magic-link-authenticator/target/keycloak-magic-link-0.9-SNAPSHOT.jar /opt/bitnami/keycloak/providers/
COPY --from=KeycloakExtension /workspace/app/iam/magic-link-authenticator/target/original-keycloak-magic-link-0.9-SNAPSHOT.jar /opt/bitnami/keycloak/providers/

COPY themes/ /opt/bitnami/keycloak/themes/

# the SMTP configuration variables
# SMTP_AUTH, SMTP_STARTTLS and SMTP_SSL are boolean that should be either true, false or an empty string
ENV SMTP_FROM=noreply@bisom.fr \
  SMTP_HOST=maildev \
  SMTP_AUTH= \
  SMTP_STARTTLS= \
  SMTP_SSL= \
  SMTP_USER= \
  SMTP_PASSWORD=

CMD ["/opt/bitnami/scripts/keycloak/run.sh"]
