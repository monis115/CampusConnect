#!/bin/bash
set -e

echo "Building the Spring Boot application..."
./mvnw clean package -DskipTests

echo "Packaging the deployment ZIP..."
rm -f target/app-deploy.zip
cp target/CampusConnect-0.0.1-SNAPSHOT.jar target/application.jar

zip -r target/app-deploy.zip target/application.jar .ebextensions Procfile

echo "Done! The artifact is ready at target/app-deploy.zip"
