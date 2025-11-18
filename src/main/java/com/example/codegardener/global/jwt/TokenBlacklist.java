package com.example.codegardener.global.jwt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String token; // 차단할 토큰

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 토큰의 원래 만료 시간 (만료 시간 지나면 DB에서 지워도 됨)
}