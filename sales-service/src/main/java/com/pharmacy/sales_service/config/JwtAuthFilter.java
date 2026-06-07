package com.pharmacy.sales_service.config;

import com.pharmacy.sales_service.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT filter for webhook endpoints (SePay, Stripe call without JWT)
        return path.startsWith("/payment/webhook");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String h = request.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            log.warn("[JwtFilter] No Bearer token for {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        try{
            String token = h.substring(7);
            Claims c = jwt.parse(token);

            List<String> roles = jwt.roles(c);
            var authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            Long uid = jwt.userId(c);
            log.info("[JwtFilter] Authenticated userId={}, roles={} for {} {}",
                    uid, roles, request.getMethod(), request.getRequestURI());
            Authentication auth = new UsernamePasswordAuthenticationToken(uid, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }catch (Exception e){
            log.error("[JwtFilter] Token parse FAILED for {} {}: {} - {}",
                    request.getMethod(), request.getRequestURI(),
                    e.getClass().getSimpleName(), e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        chain.doFilter(request, response);
    }
}
