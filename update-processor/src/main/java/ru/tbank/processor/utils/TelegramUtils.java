package ru.tbank.processor.utils;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import ru.tbank.common.telegram.CallbackEvent;
import ru.tbank.processor.excpetion.ButtonNotFoundException;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.personal.enums.ButtonTextCode;
import ru.tbank.processor.service.personal.payload.CallbackButtonPayload;
import ru.tbank.processor.service.personal.payload.CallbackData;

import java.util.List;
import java.util.stream.Collectors;

@NullMarked
@Slf4j
public final class TelegramUtils {

    public static List<CallbackButtonPayload> buildChatButtons(List<GroupChatRecord> groupChatRecords) {
        return groupChatRecords.stream()
                .map(chat -> CallbackButtonPayload.createChatButton(chat.getName(), chat.getId()))
                .collect(Collectors.toList());
    }

    public static List<CallbackButtonPayload> buildUserButtons(List<AppUserRecord> appAdmins) {
        return appAdmins.stream()
                .map(user -> CallbackButtonPayload.createUserButton(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getId()
                ))
                .collect(Collectors.toList());
    }

    public static CallbackData parseCallbackData(CallbackEvent callbackEvent) {
        String[] callbackDataArr = callbackEvent.data().split(":");
        if (!ButtonTextCode.isButton(callbackDataArr[0])) {
            throw new ButtonNotFoundException("Button with name %s not found".formatted(callbackDataArr[0]));
        }
        ButtonTextCode pressedButton = ButtonTextCode.getButtonByName(callbackDataArr[0]);
        String[] args = new String[callbackDataArr.length - 1];
        System.arraycopy(callbackDataArr, 1, args, 0, args.length);
        return new CallbackData(
                callbackEvent.messageId(),
                callbackEvent.id(),
                pressedButton,
                args
        );
    }
}
