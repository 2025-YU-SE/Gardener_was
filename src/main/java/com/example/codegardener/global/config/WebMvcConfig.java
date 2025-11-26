package com.example.codegardener.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 업로드된 파일이 저장될 로컬 경로: /uploads
    private final String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /profile-images/** 로 요청이 오면 로컬의 uploads 폴더 내용을 보여줌
        registry.addResourceHandler("/profile-images/**")
                .addResourceLocations(uploadPath);
    }
}