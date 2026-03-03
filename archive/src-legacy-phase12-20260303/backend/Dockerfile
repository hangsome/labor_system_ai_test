FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --create-home --uid 10001 appuser

WORKDIR /opt/app
COPY --from=build /workspace/target/labor-system-backend-0.0.1-SNAPSHOT.jar ./app.jar

RUN chown -R appuser:appuser /opt/app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
