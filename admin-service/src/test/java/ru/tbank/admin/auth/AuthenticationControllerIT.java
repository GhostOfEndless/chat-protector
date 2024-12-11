package ru.tbank.admin.auth;

import org.junit.jupiter.api.Test;
import ru.tbank.admin.BaseIT;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationControllerIT extends BaseIT {

    private static final String URI = "/api/v1/auth/authenticate";

    @Test
    public void simpleTest() {
        assertThat(1).isEqualTo(1);
    }
}
