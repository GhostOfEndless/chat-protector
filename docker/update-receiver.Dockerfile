FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle ./
COPY update-receiver/build.gradle ./update-receiver/build.gradle
COPY update-receiver/src ./update-receiver/src
RUN gradle clean bootJar

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/update-receiver/build/libs/*.jar .
ENTRYPOINT exec java -jar *.jar