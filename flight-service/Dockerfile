FROM gradle:7.4.2-jdk17 AS builder
WORKDIR /usr/src/app
COPY . .
RUN gradle build --no-daemon -x test

FROM openjdk:17-slim
COPY --from=builder /usr/src/app/build/libs/app.jar /app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "/app.jar"]
