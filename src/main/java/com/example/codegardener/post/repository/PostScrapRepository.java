package com.example.codegardener.post.repository;

import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.domain.PostScrap;
import com.example.codegardener.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    List<PostScrap> findAllByUser(User user);
    Optional<PostScrap> findByUserAndPost(User user, Post post);
}