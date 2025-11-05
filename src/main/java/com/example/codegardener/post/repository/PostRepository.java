package com.example.codegardener.post.repository;

import com.example.codegardener.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 1) 키워드 단건 검색 (제목/내용/작성자) — 파라미터 1개 (LIKE용)
    @Query(
            value = """
        SELECT p.*
        FROM post p
        JOIN `user` u ON u.user_id = p.user_id
        WHERE (:qLike IS NULL)
           OR LOWER(p.title)     LIKE :qLike
           OR LOWER(p.content)   LIKE :qLike
           OR LOWER(u.user_name) LIKE :qLike
        ORDER BY p.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM post p
        JOIN `user` u ON u.user_id = p.user_id
        WHERE (:qLike IS NULL)
           OR LOWER(p.title)     LIKE :qLike
           OR LOWER(p.content)   LIKE :qLike
           OR LOWER(u.user_name) LIKE :qLike
        """,
            nativeQuery = true
    )
    Page<Post> searchSimple(@Param("qLike") String qLike, Pageable pageable);

    // 2) 탭 필터
    Page<Post> findByContentsType(Boolean contentsType, Pageable pageable);

    // 3) 통합 검색 (키워드 + 언어OR + 스택OR + 탭AND + 정렬 + 페이징)
    @Query(
            value = """
        SELECT p.*
        FROM post p
        LEFT JOIN `user` u ON u.user_id = p.user_id
        WHERE
          (
            :qLike IS NULL
            OR LOWER(p.title)     LIKE :qLike
            OR LOWER(p.content)   LIKE :qLike
            OR LOWER(u.user_name) LIKE :qLike
          )
          AND ( :ct IS NULL OR p.contents_type = :ct )
          AND ( :langRegex  IS NULL OR (p.lang_tags  IS NOT NULL AND LOWER(p.lang_tags)  REGEXP :langRegex) )
          AND ( :stackRegex IS NULL OR (p.stack_tags IS NOT NULL AND LOWER(p.stack_tags) REGEXP :stackRegex) )
        ORDER BY
          CASE WHEN :sort = 'views'    THEN p.views          END DESC,
          CASE WHEN :sort = 'feedback' THEN p.feedback_count END DESC,
          p.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM post p
        LEFT JOIN `user` u ON u.user_id = p.user_id
        WHERE
          (
            :qLike IS NULL
            OR LOWER(p.title)     LIKE :qLike
            OR LOWER(p.content)   LIKE :qLike
            OR LOWER(u.user_name) LIKE :qLike
          )
          AND ( :ct IS NULL OR p.contents_type = :ct )
          AND ( :langRegex  IS NULL OR (p.lang_tags  IS NOT NULL AND LOWER(p.lang_tags)  REGEXP :langRegex) )
          AND ( :stackRegex IS NULL OR (p.stack_tags IS NOT NULL AND LOWER(p.stack_tags) REGEXP :stackRegex) )
        """,
            nativeQuery = true
    )
    Page<Post> discover(
            @Param("qLike") String qLike,
            @Param("ct") Boolean contentsType,
            @Param("langRegex") String langRegex,
            @Param("stackRegex") String stackRegex,
            @Param("sort") String sort,
            Pageable pageable
    );

    // 🔥 여기가 핵심 수정 포인트
    // Post.user.userId 기준으로 정렬
    List<Post> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // 특정 타입의 게시물 중 좋아요가 많은 상위 4개를 조회
    List<Post> findTop4ByContentsTypeOrderByLikesCountDesc(Boolean contentsType);
}