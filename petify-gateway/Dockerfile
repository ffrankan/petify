FROM amazoncorretto:17-alpine

MAINTAINER petify

VOLUME /tmp

COPY maven/petify-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]