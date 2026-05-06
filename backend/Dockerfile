# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk-noble@sha256:b866059783e3dd2dd183937b713e77d0fd94d87ce7bdd4ed4a77d79e97a276cd AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src/ src/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-noble@sha256:a051234f864d7ab78bf0188c3c540ac06c711a3b566f00f246be37073cc99dce AS jre

FROM gcr.io/distroless/java-base-debian12:nonroot@sha256:a9930cad62d02853d7f3dede7281c4b916cbf74493c2d8d38564121aad92bf6c
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/java/openjdk/bin:${PATH}"
WORKDIR /app
COPY --from=build --chown=nonroot:nonroot /app/target/*.jar app.jar
USER nonroot
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
