package ru.tbank.processor.excpetion;

public class ChatModerationSettingsNotFoundException extends ApplicationRuntimeException {

    public ChatModerationSettingsNotFoundException(String message) {
        super(message);
    }
}
