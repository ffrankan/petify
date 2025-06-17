FROM openjdk:17-jre-slim

MAINTAINER petify

VOLUME /tmp

COPY maven/petify-appointment-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]