
FROM gradle:8.5-jdk17-alpine AS builder

ARG MODULE_PATH
ARG MODULE_NAME

WORKDIR /app

COPY settings.gradle.kts gradlew ./
COPY gradle gradle


RUN chmod +x ./gradlew

COPY common common

COPY ${MODULE_PATH} ${MODULE_PATH}


RUN ./gradlew :$(echo ${MODULE_PATH} | sed 's|/|:|g'):bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG MODULE_PATH

COPY --from=builder /app/${MODULE_PATH}/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]