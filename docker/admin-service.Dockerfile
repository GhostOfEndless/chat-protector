FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle ./
COPY admin-service/build.gradle ./admin-service/build.gradle
COPY admin-service/src ./admin-service/src
COPY common/build.gradle ./common/build.gradle
COPY common/src ./common/src
RUN gradle clean bootJar

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/admin-service/build/libs/*.jar .
EXPOSE 8080
ENTRYPOINT exec java -jar *.jar