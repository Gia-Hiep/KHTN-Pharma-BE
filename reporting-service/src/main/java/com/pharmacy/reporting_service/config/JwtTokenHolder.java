package com.pharmacy.reporting_service.config;

/**
 * Thread-local holder for JWT token.
 * Populated by JwtFilter, read by internal service clients (SalesClient, InventoryClient).
 * Uses ThreadLocal instead of RequestScope for reliability in filter chain.
 */
public class JwtTokenHolder {
    private static final ThreadLocal<String> tokenStore = new ThreadLocal<>();

    public static void setToken(String token) {
        tokenStore.set(token);
    }

    public static String getToken() {
        return tokenStore.get();
    }

    public static String bearerHeader() {
        String t = tokenStore.get();
        return (t != null && !t.isBlank()) ? "Bearer " + t : "";
    }

    public static void clear() {
        tokenStore.remove();
    }
}
