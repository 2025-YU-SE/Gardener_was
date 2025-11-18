package com.example.codegardener.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 업로드된 파일이 저장될 로컬 경로 (프로젝트 루트/uploads 폴더)
    private final String uploadPath = "file:///" + System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 로 요청이 오면 로컬의 uploads 폴더 내용을 보여줌
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
    }
}