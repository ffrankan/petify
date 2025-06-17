FROM openjdk:17-jre-slim

MAINTAINER petify

VOLUME /tmp

COPY maven/petify-pet-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]