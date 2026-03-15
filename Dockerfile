# Multi-stage build per ottimizzare dimensioni
# Nota: evitiamo Alpine per prevenire crash JNI con librerie native gRPC/Netty.
FROM eclipse-temurin:17-jdk AS builder

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

# Stage finale
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia il JAR dalla fase di build
COPY --from=builder /app/target/*.jar app.jar

# Esponi la porta (Render userà quella configurata)
EXPOSE 8080

# Variabili d'ambiente di default
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Esegui l'applicazione
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
