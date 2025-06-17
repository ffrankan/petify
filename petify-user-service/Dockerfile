FROM openjdk:17-jre-slim

MAINTAINER petify

VOLUME /tmp

COPY maven/petify-user-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]