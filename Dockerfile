FROM amazoncorretto:25-al2023 AS build
# Gradle Wrapper requires xargs, provided by findutils on Amazon Linux.
RUN dnf install -y findutils && dnf clean all
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM amazoncorretto:25-al2023
WORKDIR /app
RUN dnf install -y shadow-utils && dnf clean all \
    && groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/storage/images && chown -R app:app /app
COPY --from=build --chown=app:app /workspace/build/libs/app.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
