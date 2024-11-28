package ru.tbank.processor.service.personal.enums;

public enum AdminAdditionResult {
    SUCCESS,
    USER_NOT_FOUND;

    public static boolean isAdditionResult(Object object) {
        return object instanceof AdminAdditionResult;
    }
}
