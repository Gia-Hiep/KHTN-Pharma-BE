package com.pharmacy.prescription_service.config;

import com.pharmacy.prescription_service.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if("OPTIONS".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String h = request.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims c = jwt.parse(h.substring(7));
            var authorities = jwt.roles(c).stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();

            Authentication auth = new UsernamePasswordAuthenticationToken(jwt.userId(c), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e){
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api-docs")
                || path.startsWith("/swagger")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/auth/login");
    }
}
