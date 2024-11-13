package ru.tbank.processor.utils;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.processor.excpetion.UnsupportedUpdateType;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;

import java.util.List;
import java.util.stream.Collectors;

@NullMarked
@Slf4j
public class TelegramUtils {

    private static final String BOT_ADDITION_URL = """
            https://t.me/%s?startgroup&admin=promote_members+delete_messages+restrict_members
            """;

    public static UpdateType determineUpdateType(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().isGroupMessage() || update.getMessage().isSuperGroupMessage()) {
                return UpdateType.GROUP_MESSAGE;
            } else if (update.getMessage().isUserMessage()) {
                return UpdateType.PERSONAL_MESSAGE;
            } else {
                log.warn("Unhandled type of message! {}", update);
                return UpdateType.UNKNOWN;
            }
        } else if (update.hasCallbackQuery()) {
            return UpdateType.CALLBACK;
        } else {
            log.warn("Unknown update type! {}", update);
            return UpdateType.UNKNOWN;
        }
    }

    public static User getUserFromUpdate(Update update) {
        return switch (determineUpdateType(update)) {
            case GROUP_MESSAGE, PERSONAL_MESSAGE -> update.getMessage().getFrom();
            case CALLBACK -> update.getCallbackQuery().getFrom();
            case UNKNOWN -> throw new UnsupportedUpdateType("Couldn't get user from update %s".formatted(update));
        };
    }

    public static List<CallbackButtonPayload> buildChatButtons(List<GroupChatRecord> groupChatRecords) {
        return groupChatRecords.stream()
                .map(groupChatRecord -> new CallbackButtonPayload(
                        groupChatRecord.getName(),
                        String.valueOf(groupChatRecord.getId()),
                        false
                ))
                .collect(Collectors.toList());
    }

    public static CallbackData parseCallbackWithParams(String callbackData) {
        String[] callbackDataArr = callbackData.split(":");

        ButtonTextCode pressedButton = ButtonTextCode.valueOf(callbackDataArr[0]);
        long chatId = callbackDataArr.length >= 2
                ? Long.parseLong(callbackDataArr[1])
                : 0;
        String additionalData = callbackDataArr.length >= 3
                ? callbackDataArr[2]
                : "";

        return new CallbackData(pressedButton, chatId, additionalData);
    }

    public static String createBotAdditionUrl(String botUserName) {
        return BOT_ADDITION_URL.formatted(botUserName);
    }
}
