FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY . .
RUN mvn -B -pl continew-server -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/continew-server/target/app/ /app/

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/app/bin/continew-admin.jar"]
