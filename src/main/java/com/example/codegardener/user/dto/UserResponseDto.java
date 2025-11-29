package com.example.codegardener.user.dto;

import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.domain.UserProfile;
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

    private final Long postCount;
    private final Long totalFeedbackCount;
    private final Long adoptedFeedbackCount;

    private final LocalDate lastAttendanceDate;

    private UserResponseDto(User user, long postCount, long totalFeedbackCount, long adoptedFeedbackCount) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.role = user.getRole();

        UserProfile profile = user.getUserProfile();

        if (profile != null) {
            this.userPicture = profile.getUserPicture();
            this.points = profile.getPoints();
            this.grade = profile.getGrade();
            this.lastAttendanceDate = profile.getLastAttendanceDate();
        } else {
            this.userPicture = null;
            this.points = 0;
            this.grade = null;
            this.lastAttendanceDate = null;
        }

        this.postCount = postCount;
        this.totalFeedbackCount = totalFeedbackCount;
        this.adoptedFeedbackCount = adoptedFeedbackCount;
    }

    public static UserResponseDto of(User user, long postCount, long totalFeedbackCount, long adoptedFeedbackCount) {
        return new UserResponseDto(user, postCount, totalFeedbackCount, adoptedFeedbackCount);
    }
}