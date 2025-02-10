package ru.tbank.receiver.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.tbank.common.telegram.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapCompleteUser() {
        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User(123L, "John", false);
        telegramUser.setLastName("Doe");
        telegramUser.setUserName("johndoe");
        telegramUser.setLanguageCode("en");

        User result = userMapper.toTelegramUser(telegramUser);

        assertThat(result)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.id()).isEqualTo(123L);
                    assertThat(user.firstName()).isEqualTo("John");
                    assertThat(user.lastName()).isEqualTo("Doe");
                    assertThat(user.userName()).isEqualTo("johndoe");
                    assertThat(user.languageCode()).isEqualTo("en");
                });
    }

    @Test
    void shouldMapUserWithMissingOptionalFields() {
        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User(456L, "Jane", false);

        User result = userMapper.toTelegramUser(telegramUser);

        assertThat(result)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.id()).isEqualTo(456L);
                    assertThat(user.firstName()).isEqualTo("Jane");
                    assertThat(user.lastName()).isNull();
                    assertThat(user.userName()).isNull();
                    assertThat(user.languageCode()).isNull();
                });
    }
}
