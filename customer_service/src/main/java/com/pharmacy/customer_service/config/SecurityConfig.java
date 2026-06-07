package com.pharmacy.customer_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // BUYER: own profile + own coupon/loyalty/debt
                        .requestMatchers(HttpMethod.GET, "/customers/me").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/coupons/my").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/loyalty/my").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/debts/my").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        // Admin/pharmacist full access
                        .requestMatchers("/customers/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers("/coupons/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers("/loyalty/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers("/debts/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
