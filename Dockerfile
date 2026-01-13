# 빌드 시점에 전달받을 변수 선언
ARG MODULE_PATH
ARG MODULE_NAME

# 1단계: 빌더
FROM gradle:8.5-jdk17-alpine AS builder

ARG MODULE_PATH
ARG MODULE_NAME

WORKDIR /app

# 캐시 효율을 위해 설정 파일들 먼저 복사
COPY settings.gradle.kts gradlew ./
COPY gradle gradle

# 실행 권한 부여 (윈도우 -> 리눅스 환경 이동 시 필수)
RUN chmod +x ./gradlew

# 전체 소스 복사 (common 모듈 포함)
COPY . .


RUN ./gradlew :$(echo ${MODULE_PATH} | sed 's|/|:|g'):bootJar --no-daemon -x test

# 2단계: 런타임
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 변수 재선언 (FROM이 바뀌면 ARG 초기화됨)
ARG MODULE_PATH

# 빌드된 JAR 복사
COPY --from=builder /app/${MODULE_PATH}/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]