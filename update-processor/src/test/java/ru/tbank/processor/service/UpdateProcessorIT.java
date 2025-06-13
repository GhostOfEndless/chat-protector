package ru.tbank.processor.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.common.telegram.enums.ChatType;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class UpdateProcessorIT extends AbstractBaseIT {

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        rabbitAdmin.purgeQueue("personal-updates-queue", false);
    }

    @Test
    @DisplayName("Check that bot is reacting to start command")
    void testStartCommand() {
        var user = User.builder()
                .id(123456789L)
                .firstName("First Name")
                .lastName("Last Name")
                .languageCode("ru")
                .userName("username")
                .build();
        var chat = Chat.builder()
                .id(123456789L)
                .type(ChatType.PERSONAL)
                .build();
        var message = Message.builder()
                .messageId(1)
                .text("/start")
                .user(user)
                .chat(chat)
                .build();
        var personalMessage = TelegramUpdate.createPersonalMessageUpdate(message);
        wireMock.stubFor(post("/bottoken/sendmessage")
                .willReturn(okJson(
                        """
                                {
                                   "ok": true,
                                   "result": {
                                       "message_id": 2
                                   }
                                }
                                """
                )));

        rabbitTemplate.convertAndSend("updates-exchange", "queue.personal", personalMessage);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> wireMock.verify(postRequestedFor(urlEqualTo("/bottoken/sendmessage"))));
    }
}
