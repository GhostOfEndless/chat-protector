FROM gradle:jdk21-alpine AS dependencies
WORKDIR /opt/app
ENV GRADLE_USER_HOME /cache
COPY build.gradle settings.gradle ./
COPY update-receiver/build.gradle update-receiver/build.gradle
RUN gradle :update-receiver:dependencies --no-daemon --stacktrace

FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=dependencies /cache /home/gradle/.gradle
COPY --from=dependencies $APP_HOME $APP_HOME
COPY update-receiver/src update-receiver/src
RUN gradle :update-receiver:clean :update-receiver:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:21.0.4_7-jre-alpine AS optimizer
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/update-receiver/build/libs/*.jar application-snapshot.jar
RUN java -Djarmode=tools -jar application-snapshot.jar extract --layers --launcher

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=optimizer $APP_HOME/application-snapshot/snapshot-dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/spring-boot-loader/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]