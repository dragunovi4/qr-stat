# syntax=docker/dockerfile:experimental
FROM openjdk:17-oracle as build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -DskipTests

FROM ubuntu:22.04
#openjdk:17-oracle

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
ARG DEPENDENCY=/app/target
COPY --from=build ${DEPENDENCY}/qr-stat-*-exec.jar /app/qr-stat-bot.jar
ENTRYPOINT ["java", "-jar", "/app/qr-stat-bot.jar"]