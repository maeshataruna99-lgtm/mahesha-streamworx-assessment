FROM eclipse-temurin:25.0.4.1_1-jdk-alpine AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY services services
# Tests need Docker for Testcontainers, so they run in CI and locally, not inside the image build.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -pl services/api-1-user-aggregator -am package -DskipTests

FROM eclipse-temurin:25.0.4.1_1-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/services/api-1-user-aggregator/target/api-1-user-aggregator-*.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
