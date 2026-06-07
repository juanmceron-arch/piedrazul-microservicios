# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY especialista-service/mvnw especialista-service/mvnw.cmd ./
COPY especialista-service/.mvn .mvn/
COPY especialista-service/pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY especialista-service/src ./src/
RUN ./mvnw package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S piedrazul && adduser -S piedrazul -G piedrazul
#USER piedrazul

COPY --from=build /app/target/*.jar app.jar

VOLUME /app/data

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=jdbc:sqlite:/app/data/especialista.bd"]
