#
# Build keycloak extension
#
FROM maven:3.6.3-jdk-11-slim AS KeycloakExtension
WORKDIR /workspace/app
COPY pom.xml iam/
COPY 2fa-email-authenticator/pom.xml iam/2fa-email-authenticator/
COPY api-extension/pom.xml iam/api-extension/
RUN mvn --batch-mode dependency:go-offline install -DskipTests -B -f iam

COPY 2fa-email-authenticator/src iam/2fa-email-authenticator/src
COPY api-extension/src iam/api-extension/src
RUN mvn --batch-mode install -DskipTests -f iam

FROM bitnami/keycloak:21.0.2
COPY --from=KeycloakExtension /workspace/app/iam/2fa-email-authenticator/target/keycloak-2fa-email-authenticator-1.0.0.0-SNAPSHOT.jar /opt/bitnami/keycloak/providers/
COPY 2fa-email-authenticator/src/main/resources/theme/email-code-theme/email/text/code-email.ftl /opt/bitnami/keycloak/themes/base/email/text/
COPY 2fa-email-authenticator/src/main/resources/theme/email-code-theme/email/html/code-email.ftl /opt/bitnami/keycloak/themes/base/email/html/
#RUN cat 2fa-email-authenticator/src/main/resources/theme/email-code-theme/email/messages/messages_en.properties >> /opt/bitnami/keycloak/themes/base/email/messages/messages_en.properties

COPY --from=KeycloakExtension /workspace/app/iam/api-extension/target/keycloak-search-criteria-21.0.2.jar /opt/bitnami/keycloak/providers/

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
