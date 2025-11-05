package com.example.codegardener.community.controller;

import com.example.codegardener.community.dto.MainPageResponseDto;
import com.example.codegardener.community.service.MainPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping
    public ResponseEntity<MainPageResponseDto> getMainPage(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MainPageResponseDto mainPageData = mainPageService.getMainPageData(userDetails);
        return ResponseEntity.ok(mainPageData);
    }
}