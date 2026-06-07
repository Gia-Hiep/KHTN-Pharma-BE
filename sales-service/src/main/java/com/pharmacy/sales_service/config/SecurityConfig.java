package com.pharmacy.sales_service.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        // BUYER: own orders only (owner-check enforced in service layer)
                        .requestMatchers(HttpMethod.GET,  "/orders").hasAnyRole("BUYER","ADMIN","PHARMACIST")
                        .requestMatchers(HttpMethod.GET,  "/orders/{id}").hasAnyRole("BUYER","ADMIN","PHARMACIST")
                        .requestMatchers(HttpMethod.POST, "/orders").hasAnyRole("BUYER")
                        .requestMatchers(HttpMethod.POST, "/orders/*/cancel").hasAnyRole("BUYER","ADMIN","PHARMACIST")
                        .requestMatchers(HttpMethod.POST, "/orders/*/confirm-received").hasAnyRole("BUYER","ADMIN","PHARMACIST")
                        // PHARMACIST: order management inbox
                        .requestMatchers(HttpMethod.GET,  "/pharmacist/**").hasAnyRole("PHARMACIST","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pharmacist/**").hasAnyRole("PHARMACIST","ADMIN")
                        .requestMatchers("/pharmacist/**").hasAnyRole("PHARMACIST","ADMIN")
                        // POS internal (existing): PHARMACIST/ADMIN
                        .requestMatchers("/sales/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // BUYER: Cart endpoints
                        .requestMatchers("/cart/**").hasAnyRole("BUYER")
                        .requestMatchers("/cart").hasAnyRole("BUYER")
                        // BUYER: Wallet endpoints
                        .requestMatchers("/wallet/**").hasAnyRole("BUYER")
                        .requestMatchers("/wallet").hasAnyRole("BUYER")
                        // Payment endpoints
                        .requestMatchers(HttpMethod.POST, "/payment/stripe/create-intent/*")
                            .hasAnyRole("BUYER","PHARMACIST","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/payment/stripe/create-cart-intent")
                            .hasAnyRole("BUYER","PHARMACIST","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/payment/webhook/stripe")
                            .permitAll()  // Stripe gọi không có JWT
                        .requestMatchers(HttpMethod.POST, "/payment/webhook/sepay")
                            .permitAll()  // SePay gọi không có JWT — xác thực bằng API Key
                        .requestMatchers(HttpMethod.POST, "/payment/webhook/sepay/simulate/*")
                            .permitAll()  // Simulate endpoint cho demo
                        .requestMatchers(HttpMethod.GET, "/payment/status/*")
                            .hasAnyRole("BUYER","PHARMACIST","ADMIN")  // Polling trạng thái thanh toán
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

