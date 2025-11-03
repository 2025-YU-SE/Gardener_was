package com.example.codegardener.post.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import com.example.codegardener.user.domain.User;
import com.example.codegardener.feedback.domain.Feedback;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private Boolean contentsType;

    @Column(length = 500)
    private String langTags;

    @Column(length = 500)
    private String stackTags;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(length = 300)
    private String githubRepoUrl;

    @Column(columnDefinition = "TEXT")
    private String problemStatement;

    @Column(columnDefinition = "LONGTEXT")
    private String aiFeedback;

    @Column(nullable = false)
    private int likesCount = 0;

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false)
    private int scrapCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Transient
    public int getFeedbackCount() {
        return (feedbacks != null) ? feedbacks.size() : 0;
    }

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}