# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
# pre-fetch dependencies before copying source (layer cache friendly)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -q 2>/dev/null || true

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -q

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S trading && adduser -S trading -G trading
USER trading

COPY --from=build /workspace/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-Xms256m", "-Xmx512m", \
  "-jar", "app.jar"]
