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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostScrap> scraps = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private int views = 0;
    public void increaseViews() {
        this.views++;
    }

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