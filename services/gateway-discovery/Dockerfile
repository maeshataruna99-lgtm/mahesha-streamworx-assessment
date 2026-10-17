FROM eclipse-temurin:25.0.4.1_1-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY services services
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -pl services/gateway-discovery -am package -DskipTests

FROM eclipse-temurin:25.0.4.1_1-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/services/gateway-discovery/target/gateway-discovery-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
