FROM gradle:jdk21-alpine AS dependencies
WORKDIR /opt/app
ENV GRADLE_USER_HOME=/cache
COPY build.gradle settings.gradle ./
COPY update-processor/build.gradle update-processor/build.gradle
COPY common/build.gradle common/build.gradle
COPY common-telegram/build.gradle common-telegram/build.gradle
RUN gradle :update-processor:dependencies --no-daemon --stacktrace

FROM gradle:jdk21-alpine AS builder
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME
COPY --from=dependencies /cache /home/gradle/.gradle
COPY --from=dependencies $APP_HOME $APP_HOME
COPY update-processor/src update-processor/src
COPY update-processor/jooq update-processor/jooq
COPY common/src common/src
COPY common-telegram/src common-telegram/src
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
ENTRYPOINT ["java", "-Xmx128m", "org.springframework.boot.loader.launch.JarLauncher"]