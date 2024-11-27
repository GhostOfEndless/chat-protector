package ru.tbank.processor.service.personal.enums;

import org.jspecify.annotations.NonNull;

import java.util.Map;

public enum UserRole {
    USER,
    ADMIN,
    OWNER;

    private static final Map<String, UserRole> roleNameToRole = Map.of(
            "USER", USER,
            "ADMIN", ADMIN,
            "OWNER", OWNER
    );

    public static UserRole getRoleByName(String roleName) {
        if (!roleNameToRole.containsKey(roleName)) {
            throw new IllegalArgumentException();
        }

        return roleNameToRole.get(roleName);
    }

    public boolean isEqualOrLowerThan(@NonNull UserRole userRole) {
        return this.ordinal() <= userRole.ordinal();
    }
}
