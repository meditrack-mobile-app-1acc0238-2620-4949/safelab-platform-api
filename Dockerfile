# SafeLab AV2 - Java 21 / Maven, para Render (Docker Web Service).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# Render Free puede disponer de 512 MiB RAM: reservar memoria para JVM + runtime nativo.
ENV PORT=10000 \
    SPRING_PROFILES_ACTIVE=postgres \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=15.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"
COPY --from=build /build/target/safelab-platform-api-2.5.1.jar /app/app.jar
USER 10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
