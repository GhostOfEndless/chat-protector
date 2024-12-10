FROM gradle:jdk21-alpine AS dependencies
WORKDIR /opt/app
ENV GRADLE_USER_HOME=/cache
COPY build.gradle settings.gradle ./
COPY admin-service/build.gradle admin-service/build.gradle
COPY common/build.gradle common/build.gradle
RUN gradle :admin-service:dependencies --no-daemon --stacktrace

FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=dependencies /cache /home/gradle/.gradle
COPY --from=dependencies $APP_HOME $APP_HOME
COPY admin-service/src admin-service/src
COPY admin-service/jooq admin-service/jooq
COPY common/src common/src
RUN gradle :admin-service:clean :admin-service:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:21.0.4_7-jre-alpine AS optimizer
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=builder $APP_HOME/admin-service/build/libs/*.jar application-snapshot.jar
RUN java -Djarmode=tools -jar application-snapshot.jar extract --layers --launcher

FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=optimizer $APP_HOME/application-snapshot/snapshot-dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/spring-boot-loader/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/dependencies/ ./
COPY --from=optimizer $APP_HOME/application-snapshot/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx128m", "org.springframework.boot.loader.launch.JarLauncher"]