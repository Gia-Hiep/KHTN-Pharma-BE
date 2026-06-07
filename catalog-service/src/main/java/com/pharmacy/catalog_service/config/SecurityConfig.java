package com.pharmacy.catalog_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Serve uploaded images publicly
                        .requestMatchers("/uploads/**").permitAll()

                        // Swagger / OpenAPI (tùy lib bạn dùng, mình để nhiều pattern cho chắc)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger/**"
                        ).permitAll()

                        // BUYER read-only: list, search medicines, view detail, view active prices
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/search").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/smart-search").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/*/images/**").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/*").hasAnyRole("ADMIN","PHARMACIST","BUYER") // /{id}
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/code/*").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/medicines/barcode/*").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/pricing/medicine/*/active").hasAnyRole("ADMIN","PHARMACIST","BUYER")

                        // Disease groups & categories: read for BUYER too
                        .requestMatchers(HttpMethod.GET, "/catalog/disease-groups/**").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/catalog/categories").hasAnyRole("ADMIN","PHARMACIST","BUYER")

                        // Everything else under /catalog/**: ADMIN/PHARMACIST only
                        .requestMatchers("/catalog/**").hasAnyRole("ADMIN","PHARMACIST")

                        // default
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
