# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY appointment-service/mvnw appointment-service/mvnw.cmd ./
COPY appointment-service/.mvn .mvn/
COPY appointment-service/pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY appointment-service/src ./src/
RUN ./mvnw package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S piedrazul && adduser -S piedrazul -G piedrazul
#USER piedrazul

COPY --from=build /app/target/*.jar app.jar

VOLUME /app/data

EXPOSE 8082

# Las URLs de otros servicios se inyectan como variables de entorno desde docker-compose
# El código ya usa @Value("${services.especialista.url}") y @Value("${services.auth.url}")
# así que solo hace falta pasarlas aquí — sin tocar el código fuente.
ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=jdbc:sqlite:/app/data/appointmentBD.db"]
