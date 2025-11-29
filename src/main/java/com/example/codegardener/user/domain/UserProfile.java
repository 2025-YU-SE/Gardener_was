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

    @Id
    @Column(name = "user_profile_id")
    private Long userProfileId;

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

    @Column(name = "last_attendance_date")
    private LocalDate lastAttendanceDate;
}