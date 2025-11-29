package com.example.codegardener.post.repository;

import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.domain.PostLike;
import com.example.codegardener.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
}