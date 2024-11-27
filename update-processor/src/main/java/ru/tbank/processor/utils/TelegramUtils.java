package ru.tbank.processor.utils;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tbank.processor.excpetion.UnsupportedUpdateType;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;
import ru.tbank.processor.utils.enums.ChatType;
import ru.tbank.processor.utils.enums.UpdateType;

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
            return determineMessageType(update);
        } else if (update.hasMyChatMember()) {
            return determineChatMemberEvent(update);
        } else if (update.hasCallbackQuery()) {
            return UpdateType.CALLBACK;
        } else {
            log.warn("Unknown update type! {}", update);
            return UpdateType.UNKNOWN;
        }
    }

    public static ChatType determineChatType(UpdateType updateType) {
        return switch (updateType) {
            case GROUP_BOT_ADDED, GROUP_BOT_KICKED, GROUP_MESSAGE, GROUP_BOT_LEFT -> ChatType.GROUP;
            case CALLBACK, PERSONAL_MESSAGE -> ChatType.PERSONAL;
            case UNKNOWN -> ChatType.UNKNOWN;
        };
    }

    public static User getUserFromUpdate(Update update) {
        return switch (determineUpdateType(update)) {
            case GROUP_BOT_ADDED, GROUP_BOT_KICKED, GROUP_BOT_LEFT -> update.getMyChatMember().getFrom();
            case GROUP_MESSAGE, PERSONAL_MESSAGE -> update.getMessage().getFrom();
            case CALLBACK -> update.getCallbackQuery().getFrom();
            case UNKNOWN -> throw new UnsupportedUpdateType("Couldn't get user from update %s".formatted(update));
        };
    }

    public static List<CallbackButtonPayload> buildChatButtons(List<GroupChatRecord> groupChatRecords) {
        return groupChatRecords.stream()
                .map(chat -> CallbackButtonPayload.create(chat.getName(), chat.getId()))
                .collect(Collectors.toList());
    }

    public static CallbackData parseCallbackData(CallbackQuery callbackQuery) {
        String[] callbackDataArr = callbackQuery.getData().split(":");
        String[] args = new String[callbackDataArr.length - 1];
        System.arraycopy(callbackDataArr, 1, args, 0, args.length);
        ButtonTextCode pressedButton = ButtonTextCode.valueOf(callbackDataArr[0]);

        return new CallbackData(
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getId(),
                pressedButton,
                args
        );
    }

    public static String createBotAdditionUrl(String botUserName) {
        return BOT_ADDITION_URL.formatted(botUserName);
    }

    private static UpdateType determineMessageType(Update update) {
        if (update.getMessage().isGroupMessage() || update.getMessage().isSuperGroupMessage()) {
            return UpdateType.GROUP_MESSAGE;
        } else if (update.getMessage().isUserMessage()) {
            return UpdateType.PERSONAL_MESSAGE;
        } else {
            log.warn("Unhandled type of message! {}", update);
            return UpdateType.UNKNOWN;
        }
    }

    private static UpdateType determineChatMemberEvent(Update update) {
        if (update.getMyChatMember().getChat().isGroupChat() || update.getMyChatMember().getChat().isSuperGroupChat()) {
            String oldStatus = update.getMyChatMember().getOldChatMember().getStatus();
            String newStatus = update.getMyChatMember().getNewChatMember().getStatus();
            if ((oldStatus.equals("left") || oldStatus.equals("kicked"))
                    && (newStatus.equals("administrator") || newStatus.equals("member"))) {
                return UpdateType.GROUP_BOT_ADDED;
            } else if (newStatus.equals("kicked") || newStatus.equals("left")) {
                return update.getMyChatMember().getFrom().getIsBot()
                        ? UpdateType.GROUP_BOT_LEFT
                        : UpdateType.GROUP_BOT_KICKED;
            }
        }
        log.warn("Unknown chat member update type! {}", update);
        return UpdateType.UNKNOWN;
    }
}
