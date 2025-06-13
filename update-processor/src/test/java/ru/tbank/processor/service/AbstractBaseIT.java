package ru.tbank.processor.service;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.redis.testcontainers.RedisContainer;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.processor.ProcessorApplication;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
@SpringBootTest(classes = {
        ProcessorApplication.class,
        AbstractBaseIT.ITConfiguration.class
})
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractBaseIT {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    private static final RedisContainer redisMaster = new RedisContainer("redis:7.4.1");
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.1-rc-management-alpine");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensions(new ServeEventListener() {
                @Override
                public String getName() {
                    return "request-logger";
                }

                @Override
                public void beforeMatch(ServeEvent serveEvent, Parameters parameters) {
                    log.info("Received request: {} {}", serveEvent.getRequest().getMethod(), serveEvent.getRequest().getUrl());
                    log.info("Body: {}", serveEvent.getRequest().getBodyAsString());
                }
            }))
            .build();

    @Autowired
    protected RabbitTemplate rabbitTemplate;
    @Autowired
    protected RabbitAdmin rabbitAdmin;

    @SneakyThrows
    @DynamicPropertySource
    static void postgresqlProperties(@NonNull DynamicPropertyRegistry registry) {
        postgres.start();
        runMigrations();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @DynamicPropertySource
    static void redisProperties(@NonNull DynamicPropertyRegistry registry) {
        redisMaster.start();
        registry.add("redis.master.host", redisMaster::getRedisHost);
        registry.add("redis.master.port", redisMaster::getRedisPort);
        registry.add("redis.slaves", Collections::emptyList);
    }

    @SneakyThrows
    @DynamicPropertySource
    static void rebbitProperties(@NonNull DynamicPropertyRegistry registry) {
        rabbit.start();
        rabbit.execInContainer("rabbitmqctl", "add_vhost", "vhost");
        rabbit.execInContainer("rabbitmqctl", "add_user", "user", "password");
        rabbit.execInContainer("rabbitmqctl", "set_permissions", "-p", "vhost", "user", ".*", ".*", ".*");
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @DynamicPropertySource
    static void wiremockPropertiest(@NonNull DynamicPropertyRegistry registry) {
        registry.add("telegram.port", wireMock::getPort);
    }

    private static void runMigrations() throws Exception {
        Path path = new File(".").toPath().toAbsolutePath().getParent().getParent()
                .resolve("migrations/db/changelog/");
        Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(
                "db-changelog.yml",
                new DirectoryResourceAccessor(path),
                database
        );

        liquibase.update(new Contexts(), new LabelExpression());
    }

    @TestConfiguration
    static class ITConfiguration {

        @Bean
        public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
            return new RabbitAdmin(rabbitTemplate);
        }
    }
}
