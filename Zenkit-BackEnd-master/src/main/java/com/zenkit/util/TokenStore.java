package com.zenkit.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStore {
    private static final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    public static void storeToken(String email, String code) {
        resetTokens.put(email, code);
    }

    public static boolean verifyToken(String email, String code) {
        return code.equals(resetTokens.get(email));
    }

    public static void removeToken(String email) {
        resetTokens.remove(email);
    }
}
