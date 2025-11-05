package com.example.codegardener.user.domain;

import java.time.LocalDate;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    public static final String GRADE_DELETED = "탈퇴한 사용자";

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_picture", length = 300)
    private String userPicture;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(length = 20)
    private String grade;

    @Column(name = "post_count",  nullable = false)
    private Integer postCount = 0;

    @Column(name = "total_feedback_count", nullable = false)
    private Integer totalFeedbackCount = 0;

    @Column(name = "adopted_feedback_count", nullable = false)
    private Integer adoptedFeedbackCount = 0;

    @Column(name = "last_attendance_date")
    private LocalDate lastAttendanceDate;
}