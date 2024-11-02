package ru.tbank.processor.service.personal;

public enum UserRole {
    USER,
    ADMIN,
    OWNER;

    public static int getRoleLevel(String roleName) {
        try {
            return valueOf(roleName).ordinal();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}
