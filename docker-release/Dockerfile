FROM eclipse-temurin:25-jre-alpine

COPY app/ /app

WORKDIR /local
ENTRYPOINT ["java", "-jar", "/app/replace-php-serialize-safe.jar"]
