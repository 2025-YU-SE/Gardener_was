package com.example.codegardener.user.dto;

import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.domain.UserProfile; // UserProfile 임포트

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserResponseDto {

    private final Long userId;
    private final String userName;
    private final String email;
    private final Role role;

    private final String userPicture;
    private final Integer points;
    private final String grade;
    private final Integer postCount;
    private final Integer totalFeedbackCount;
    private final Integer adoptedFeedbackCount;
    private final LocalDate lastAttendanceDate;

    private UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.role = user.getRole();

        UserProfile profile = user.getUserProfile();

        if (profile != null) {
            this.userPicture = profile.getUserPicture();
            this.points = profile.getPoints();
            this.grade = profile.getGrade();
            this.postCount = profile.getPostCount();
            this.totalFeedbackCount = profile.getTotalFeedbackCount();
            this.adoptedFeedbackCount = profile.getAdoptedFeedbackCount();
            this.lastAttendanceDate = profile.getLastAttendanceDate();
        } else {
            this.userPicture = null;
            this.points = 0;
            this.grade = null;
            this.postCount = 0;
            this.totalFeedbackCount = 0;
            this.adoptedFeedbackCount = 0;
            this.lastAttendanceDate = null;
        }
    }

    public static UserResponseDto fromEntity(User user) {
        if (user == null) return null;
        return new UserResponseDto(user);
    }
}