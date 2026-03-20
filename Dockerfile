FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Cloud Run sets PORT env var dynamically - Spring Boot must use it
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", \
  "-Dserver.port=${PORT}", \
  "-Dspring.cloud.config.fail-fast=false", \
  "-Dspring.cloud.config.enabled=false", \
  "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", \
  "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", \
  "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", \
  "-Dspring.jpa.hibernate.ddl-auto=update", \
  "-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect", \
  "-Dgcp.storage.bucket-name=${GCP_STORAGE_BUCKET_NAME}", \
  "-Dgcp.storage.project-id=${GCP_STORAGE_PROJECT_ID}", \
  "-Deureka.client.service-url.defaultZone=${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}", \
  "-Deureka.instance.prefer-ip-address=true", \
  "-Dspring.config.import=", \
  "-jar", "app.jar"]
