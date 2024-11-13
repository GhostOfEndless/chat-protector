FROM gradle:jdk21-alpine AS dependencies
WORKDIR /opt/app
ENV GRADLE_USER_HOME /cache
COPY build.gradle settings.gradle ./
COPY update-processor/build.gradle update-processor/build.gradle
COPY common/build.gradle common/build.gradle
RUN gradle :update-processor:dependencies --no-daemon --stacktrace

FROM gradle:jdk21-alpine AS builder
ARG POSTGRES_HOST
ARG POSTGRES_DB
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ENV DB_HOST $POSTGRES_HOST
ENV DB_PORT $POSTGRES_PORT
ENV DB_NAME $POSTGRES_DB
ENV DB_USER $POSTGRES_USER
ENV DB_PASSWORD $POSTGRES_PASSWORD
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=dependencies /cache /home/gradle/.gradle
COPY --from=dependencies $APP_HOME $APP_HOME
COPY update-processor/src update-processor/src
COPY common/src common/src
RUN gradle :update-processor:clean :update-processor:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:21.0.4_7-jre-alpine AS optimizer
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/update-processor/build/libs/*.jar application-snapshot.jar
RUN java -Djarmode=tools -jar application-snapshot.jar extract --layers --launcher

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=optimizer $APP_HOME/application-snapshot/snapshot-dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/spring-boot-loader/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]