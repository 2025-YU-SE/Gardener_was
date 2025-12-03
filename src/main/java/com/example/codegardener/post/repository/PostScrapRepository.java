package com.example.codegardener.post.repository;

import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.domain.PostScrap;
import com.example.codegardener.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    List<PostScrap> findAllByUser(User user);
    Optional<PostScrap> findByUserAndPost(User user, Post post);

    // 사용자가 스크랩한 게시물 페이징
    Page<PostScrap> findAllByUser(User user, Pageable pageable);
    // 사용자가 최근 스크랩한 게시물 4개
    List<PostScrap> findFirst4ByUserOrderByCreatedAtDesc(User user);

    long countByPost(Post post);
    boolean existsByUserAndPost(User user, Post post);
}