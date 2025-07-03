FROM eclipse-temurin:17-jdk-alpine

# 시간대 설정을 위한 tzdata 설치 및 적용
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && apk del tzdata  # (옵션) tzdata 제거하여 이미지 사이즈 줄이기

WORKDIR /app
COPY build/libs/*jar app.jar

# JVM 시간대도 명시
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
