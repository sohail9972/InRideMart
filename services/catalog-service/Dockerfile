FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY services/catalog-service/pom.xml services/catalog-service/pom.xml
COPY services/catalog-service/src services/catalog-service/src
RUN mvn -pl services/catalog-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/services/catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
