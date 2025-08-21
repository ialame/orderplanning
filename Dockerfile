# ===============================================
# DOCKERFILE BACKEND SPRING BOOT
# ===============================================
# Fichier: Dockerfile.backend (racine du projet)

# ========== ÉTAPE 1: BUILD ==========
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY src ./src

# Compiler l'application
RUN mvn clean package -DskipTests

# ========== ÉTAPE 2: RUNTIME ==========
FROM eclipse-temurin:21-jre

WORKDIR /app

# Créer un utilisateur non-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copier le JAR depuis l'étape build
COPY --from=builder /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown appuser:appuser app.jar

# Passer à l'utilisateur non-root
USER appuser

# Exposer le port
EXPOSE 8080

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/employees/debug || exit 1

# Commande de démarrage
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]