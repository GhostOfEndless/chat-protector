package ru.tbank.admin.service.settings;

import java.util.List;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ru.tbank.admin.controller.moderation.payload.TextFilterSettingsRequest;
import ru.tbank.admin.exceptions.ExclusionValidationException;
import ru.tbank.common.entity.enums.FilterType;

@Component
public class TextFilterSettingsExcludesValidator {

    private static final Pattern TAG_PATTERN = Pattern.compile("^#[a-zA-Z0-9_]+$");
    private static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[1-9]\\d{10,14}$");
    private static final Pattern BOT_COMMAND_PATTERN = Pattern.compile("^/[a-zA-Z0-9_]{1,32}$");
    private static final Pattern MENTION_PATTERN = Pattern.compile("^@[a-zA-Z0-9_]{4,32}$");
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "^(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$"
    );

    public void validate(@NonNull FilterType filterType, @NonNull TextFilterSettingsRequest request) {
        switch (filterType) {
            case TAGS -> validateExclusions(request.exclusions(), TAG_PATTERN);
            case BOT_COMMANDS -> validateExclusions(request.exclusions(), BOT_COMMAND_PATTERN);
            case CUSTOM_EMOJIS -> validateExclusions(request.exclusions(), CUSTOM_EMOJI_PATTERN);
            case EMAILS -> validateExclusions(request.exclusions(), EMAIL_PATTERN);
            case LINKS -> validateExclusions(request.exclusions(), LINK_PATTERN);
            case MENTIONS -> validateExclusions(request.exclusions(), MENTION_PATTERN);
            case PHONE_NUMBERS -> validateExclusions(request.exclusions(), PHONE_NUMBER_PATTERN);
            default -> throw new IllegalArgumentException("Unsupported filter type");
        }
    }

    private void validateExclusions(@NonNull List<String> exclusions, Pattern pattern) {
        exclusions.forEach(exclusion -> {
            if (!pattern.matcher(exclusion).matches()) {
                throw new ExclusionValidationException(exclusion);
            }
        });
    }
}
