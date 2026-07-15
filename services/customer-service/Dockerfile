FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY services/customer-service/pom.xml services/customer-service/pom.xml
COPY services/customer-service/src services/customer-service/src
RUN mvn -pl services/customer-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/services/customer-service/target/customer-service-0.1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
