package com.example.codegardener.community.dto;

import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MainPageResponseDto {
    private UserResponseDto userInfo; // 로그인한 사용자 정보 (비로그인 시 null)
    private List<UserResponseDto> topPointUsers;
    private List<PostResponseDto> popularDevPosts; // 인기 개발 게시물
    private List<PostResponseDto> popularCodingTestPosts; // 인기 코테 게시물
}