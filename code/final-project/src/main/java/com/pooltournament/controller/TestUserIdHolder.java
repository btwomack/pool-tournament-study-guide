package com.pooltournament.controller;

import java.util.UUID;

public class TestUserIdHolder {
    private static final ThreadLocal<UUID> userIdHolder = new ThreadLocal<>();

    public static void setUserId(UUID userId) {
        userIdHolder.set(userId);
    }

    public static UUID getUserId() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
