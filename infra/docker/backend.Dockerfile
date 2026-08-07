FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY backend/src ./src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
