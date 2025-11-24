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

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 🔥 반드시 추가
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // OPTIONS(Preflight) 무조건 허용 ⭐⭐⭐⭐⭐
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // public endpoints
                        .requestMatchers(
                                "/api/user/signup",
                                "/api/user/login",
                                "/api/user/check-username",
                                "/api/user/check-email"
                        ).permitAll()

                        // GET public
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/*",
                                "/api/posts/search",
                                "/api/feedback/post/*",
                                "/api/feedback/*",
                                "/api/leaderboard/**",
                                "/api/main"
                        ).permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/api/user/{userId}/admin").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * 🔹 CORS 규칙 정의
     *  - 어디(origin)에서 오는 요청을 허용할지
     *  - 어떤 메서드/헤더를 허용할지
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 개발 서버 Origin
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드들
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 어떤 헤더를 허용할지 (Authorization, Content-Type 등)
        config.setAllowedHeaders(List.of("*"));

        // 인증정보(쿠키, Authorization 헤더 등) 포함 허용
        config.setAllowCredentials(true);

        // 모든 경로에 위 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}