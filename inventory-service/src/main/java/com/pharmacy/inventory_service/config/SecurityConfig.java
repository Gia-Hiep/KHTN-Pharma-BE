package com.pharmacy.inventory_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // READ endpoints: all authenticated users
                        .requestMatchers(HttpMethod.GET, "/inventory/summary").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/inventory/lots").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/inventory/lots/**").hasAnyRole("ADMIN","PHARMACIST","BUYER")
                        .requestMatchers(HttpMethod.GET, "/inventory/alerts/**").hasAnyRole("ADMIN","PHARMACIST")
                        .requestMatchers(HttpMethod.GET, "/inventory/transactions").hasAnyRole("ADMIN","PHARMACIST")
                        // Write ops (reserve/commit/release/adjust): PHARMACIST/ADMIN only
                        .requestMatchers(HttpMethod.POST,
                                "/inventory/commit",
                                "/inventory/reserve",
                                "/inventory/release",
                                "/inventory/return-stock",
                                "/inventory/adjust-reservation")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

