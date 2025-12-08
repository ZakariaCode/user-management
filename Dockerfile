# Utiliser une image de base OpenJDK
FROM eclipse-temurin:21-jdk-alpine

# Créer un répertoire de travail
WORKDIR /app

# Copier le fichier jar généré dans le conteneur Docker
COPY target/user-management.jar app.jar

# Exposer le port sur lequel l'application sera disponible
EXPOSE 8080

# Commande pour exécuter l'application
ENTRYPOINT ["java", "-jar", "app.jar"]