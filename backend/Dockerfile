# syntax=docker/dockerfile:1

# --- Stage 1: build del jar ---------------------------------------------------
# JDK 21 fisso: Lombok 1.18.32 non compila su JDK 25 (vedi note di progetto).
# Il pom ha <java.version>17>, quindi il bytecode prodotto e' Java 17.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Prima solo il pom: lo strato delle dipendenze si ricostruisce solo se cambia il pom.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Poi i sorgenti. I test girano nella pipeline CI, qui li saltiamo per velocita'.
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# --- Stage 2: runtime ---------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Utente non-root
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Solo l'eseguibile repackaged (il glob esclude il .jar.original di Spring Boot).
COPY --from=build /app/target/*.jar app.jar

# Config via env: nessun segreto e' baked nell'immagine.
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
