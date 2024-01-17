FROM openjdk:17-slim

WORKDIR /app

COPY airport-service-artifact/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
