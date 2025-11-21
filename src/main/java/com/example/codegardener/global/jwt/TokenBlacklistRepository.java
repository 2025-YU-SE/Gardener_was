package com.example.codegardener.global.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    // 해당 토큰이 블랙리스트에 있는지 확인
    boolean existsByToken(String token);
}