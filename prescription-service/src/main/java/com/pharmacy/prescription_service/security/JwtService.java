package com.pharmacy.prescription_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    public Claims parse(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims c){
        List<?> raw = (List<?>) c.get("roles");
        if (raw == null) return List.of();
        return raw.stream().map(r -> r.toString().trim()).collect(Collectors.toList());
    }

    public Long userId(Claims c){
        Object v = c.get("userId");
        if (v == null) return null;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l) return l;
        if (v instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("Invalid userId claim type: " + v.getClass());
    }
}

