# 1. 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 의존성 파일 먼저 복사하여 캐싱 활용
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 코드 복사 및 패키징
COPY src ./src
RUN mvn clean package -DskipTests

# 2. 실행 스테이지
# 배포용 (인텔칩 전용)
# FROM eclipse-temurin:17-jre-alpine

# 로컬 개발용(Windows && Mac)
FROM eclipse-temurin:17-jre 
WORKDIR /app

# 시스템에 필요한 도구 설치 (curl: 헬스체크용) (배포용)
# RUN apk add --no-cache curl 

# 로컬 개발용(Mac)
RUN apt-get update && apt-get install -y curl

# 파일 업로드 디렉토리 준비
RUN mkdir -p /app/uploads && chmod 755 /app/uploads

# 빌드된 JAR 파일 복사
COPY --from=build /app/target/*.jar app.jar

# 환경 변수 기본값 설정 (컨테이너 실행 시 오버라이딩 가능)
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul

EXPOSE 8080

# 서비스 상태 체크 (실제 존재하는 엔드포인트여야 함)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#   CMD curl -f http://localhost:8080/api/notices?page=0&size=1 || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
