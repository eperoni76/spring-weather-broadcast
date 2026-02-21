# Multi-stage build per ottimizzare dimensioni
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copia i file Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Scarica le dipendenze (layer cachabile)
RUN ./mvnw dependency:go-offline

# Copia il codice sorgente
COPY src ./src

# Build dell'applicazione
RUN ./mvnw clean package -DskipTests

# Stage finale - immagine più leggera
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia il JAR dalla fase di build
COPY --from=builder /app/target/*.jar app.jar

# Esponi la porta (Render userà quella configurata)
EXPOSE 8080

# Variabili d'ambiente di default
ENV JAVA_OPTS="-Xmx512m -Xms256m"

#Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Esegui l'applicazione
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
