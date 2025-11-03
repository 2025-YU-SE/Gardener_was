package com.example.codegardener.user.dto;

import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.User;

import lombok.Getter;

@Getter
public class UserResponseDto {

    private final Long id;
    private final String userName;
    private final String email;
    private final Role role;

    private final String userPicture;
    private final Integer points;
    private final String grade;
    private final Integer postCount;
    private final Integer totalFeedbackCount;
    private final Integer adoptedFeedbackCount;
    private final String lastAttendanceDate;

    public UserResponseDto(User user) {

        this.id = user.getId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.role = user.getRole();

        if (user.getUserProfile() != null) {
            this.userPicture = user.getUserProfile().getUserPicture();
            this.points = user.getUserProfile().getPoints();
            this.grade = user.getUserProfile().getGrade();
            this.postCount = user.getUserProfile().getPostCount();
            this.totalFeedbackCount = user.getUserProfile().getTotalFeedbackCount();
            this.adoptedFeedbackCount = user.getUserProfile().getAdoptedFeedbackCount();
            this.lastAttendanceDate = user.getUserProfile().getLastAttendanceDate();
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
}