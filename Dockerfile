FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean bootJar \
    && cp build/libs/*.jar /app/app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --create-home --home-dir /app spring

COPY --from=builder /app/app.jar /app/app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
