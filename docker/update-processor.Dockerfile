FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle ./
COPY update-processor/build.gradle ./update-processor/build.gradle
COPY update-processor/src ./update-processor/src
COPY common/build.gradle ./common/build.gradle
COPY common/src ./common/src
RUN gradle clean bootJar

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/update-processor/build/libs/*.jar .
ENTRYPOINT exec java -jar *.jar