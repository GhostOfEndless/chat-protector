package ru.tbank.admin;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

@AutoConfigureMockMvc
@SpringBootTest(classes = AdminServiceApplication.class)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIT {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    private static final RedisContainer redisMaster = new RedisContainer("redis:7.4.1");
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

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
}
