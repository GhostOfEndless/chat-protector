package ru.tbank.admin.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import ru.tbank.admin.entity.ApplicationUser;
import ru.tbank.admin.exceptions.UserNotFoundException;
import ru.tbank.admin.generated.tables.AppUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {

    private final static AppUser table = AppUser.APP_USER;
    private final DSLContext dslContext;

    public ApplicationUser getByUsername(String username) {
        var user = dslContext.fetchOptional(table, table.USERNAME.eq(username))
                .orElseThrow(() -> new UserNotFoundException(username));
        return user.into(ApplicationUser.class);
    }
}
