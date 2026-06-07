# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY auth-service/mvnw auth-service/mvnw.cmd ./
COPY auth-service/.mvn .mvn/
COPY auth-service/pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY auth-service/src ./src/
RUN ./mvnw package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S piedrazul && adduser -S piedrazul -G piedrazul
#USER piedrazul

COPY --from=build /app/target/*.jar app.jar

VOLUME /app/data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=jdbc:sqlite:/app/data/auth.db"]
