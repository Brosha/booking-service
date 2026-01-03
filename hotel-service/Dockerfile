FROM eclipse-temurin:17-jre

ARG JAR_FILE=target/hotel-service-1.0-SNAPSHOT.jar
WORKDIR /app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

