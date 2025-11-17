package com.example.codegardener.global.config;

import com.example.codegardener.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔹 Swagger / OpenAPI 문서 경로 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 🔹 인증 없이 항상 허용할 경로
                        .requestMatchers("/api/user/signup", "/api/user/login").permitAll()

                        // 🔹 인증 없이 허용할 공개 GET 경로
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/*",      // /api/posts/{id} 대신 패턴으로
                                "/api/posts/search",
                                "/api/feedback/post/*",
                                "/api/feedback/*",
                                "/api/leaderboard/**",
                                "/api/main"
                        ).permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/api/user/{userId}/admin").hasRole("ADMIN")

                        // 🔹 그 외 "모든 요청"은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}