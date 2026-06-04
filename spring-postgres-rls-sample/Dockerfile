FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the entire project so we can build the starter and the sample together
COPY . .

# Build the whole project (starter + sample)
RUN mvn clean package -DskipTests

# -------------------
# Runtime image
# -------------------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the compiled sample app jar
COPY --from=build /app/spring-postgres-rls-sample/target/spring-postgres-rls-sample.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]