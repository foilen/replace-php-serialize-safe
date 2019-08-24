FROM openjdk:11-jre-slim

COPY app/ /app

WORKDIR /local
ENTRYPOINT ["java", "-jar", "/app/replace-php-serialize-safe.jar"]
