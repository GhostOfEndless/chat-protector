package ru.tbank.admin.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.tbank.admin.exceptions.UserNotFoundException;
import ru.tbank.admin.exceptions.UsernameNotFoundException;
import ru.tbank.admin.generated.tables.AppUser;
import ru.tbank.admin.pojo.ApplicationUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {

    private final static AppUser TABLE = AppUser.APP_USER;
    private final DSLContext dslContext;

    public ApplicationUser getByUsername(String username) {
        var user = dslContext.fetchOptional(TABLE, TABLE.USERNAME.eq(username))
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return user.into(ApplicationUser.class);
    }

    public ApplicationUser getById(Long id) {
        var user = dslContext.fetchOptional(TABLE, TABLE.ID.eq(id))
                .orElseThrow(() -> new UserNotFoundException(id));
        return user.into(ApplicationUser.class);
    }

    public Page<ApplicationUser> getApplicationUsers(@NonNull Pageable pageable) {
        var records = dslContext.selectFrom(TABLE)
                .orderBy(TABLE.ADDITION_DATE.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .into(ApplicationUser.class);

        return new PageImpl<>(records);
    }

    public boolean existsById(Long id) {
        return dslContext.fetchOptional(TABLE, TABLE.ID.eq(id)).isPresent();
    }
}
