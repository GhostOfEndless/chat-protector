package ru.tbank.processor.service.personal;

import org.jspecify.annotations.NonNull;

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

    public boolean isEqualOrLowerThan(@NonNull UserRole userRole) {
        return this.ordinal() <= userRole.ordinal();
    }
}
