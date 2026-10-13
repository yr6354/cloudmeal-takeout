# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷贝 pom，利用 Docker 层缓存避免每次改动代码都重新下载依赖
COPY pom.xml .
COPY cloudmeal-common/pom.xml cloudmeal-common/
COPY cloudmeal-model/pom.xml cloudmeal-model/
COPY cloudmeal-web/pom.xml cloudmeal-web/
RUN mvn -B -ntp dependency:go-offline

COPY cloudmeal-common/src cloudmeal-common/src
COPY cloudmeal-model/src cloudmeal-model/src
COPY cloudmeal-web/src cloudmeal-web/src
RUN mvn -B -ntp clean package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

COPY --from=builder /build/cloudmeal-web/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
