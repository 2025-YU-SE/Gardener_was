package com.example.codegardener.post.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.codegardener.post.dto.PostRequestDto;
import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.post.service.PostService;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ====================== CREATE ======================
    @PostMapping
    public ResponseEntity<PostResponseDto> create(
            @Valid @RequestBody PostRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("게시물 생성 권한 인증이 필요합니다.");
        }

        PostResponseDto created = postService.create(dto, userDetails.getUsername());

        return ResponseEntity
                .created(URI.create("/posts/" + created.getPostId()))
                .body(created);
    }

    // ====================== READ ======================

    /** 게시물 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.get(id));
    }

    /**
     * 페이징 목록
     * contentsType: null=전체 / true=개발 / false=코테
     * order: recent(최신순), popular(조회수), feedback(피드백순)
     */
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPostList(
            @RequestParam(required = false) Boolean contentsType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "order", defaultValue = "recent") String order
    ) {
        String sortBy = mapOrderToSortKey(order);
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);

        Page<PostResponseDto> postPage =
                postService.getPostList(safePage, safeSize, contentsType, sortBy);

        return ResponseEntity.ok(postPage);
    }

    // ====================== UPDATE ======================
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("수정 권한 인증이 필요합니다.");
        }
        PostResponseDto updated = postService.update(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // ====================== DELETE ======================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("삭제 권한 인증이 필요합니다.");
        }
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ====================== SEARCH (통합 검색) ======================
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponseDto>> searchUnified(
            @RequestParam(required = false) String q,
            @RequestParam(name = "languages", required = false) List<String> languages,
            @RequestParam(name = "langs", required = false) String langsCsv,
            @RequestParam(name = "stacks", required = false) List<String> stacks,
            @RequestParam(name = "tech", required = false) String stacksCsv,
            @RequestParam(required = false) Boolean contentsType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "order", defaultValue = "recent") String order
    ) {
        String sort = switch (order.toLowerCase()) {
            case "popular"  -> "views";
            case "feedback" -> "feedback";
            default         -> "latest";
        };

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<PostResponseDto> result = postService.discoverAdvanced(
                q, languages, langsCsv, stacks, stacksCsv, contentsType,
                safePage, safeSize, sort
        );

        return ResponseEntity.ok(result);
    }

    // ====================== AI 피드백 ======================
    @PostMapping("/{id}/ai")
    public ResponseEntity<PostResponseDto> regenerateAi(
            @PathVariable Long id,
            @RequestParam(required = false) Long requesterId
    ) {
        PostResponseDto dto = postService.generateAiFeedback(id, requesterId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/ai")
    public ResponseEntity<String> getAiFeedback(@PathVariable Long id) {
        String feedback = postService.getAiFeedback(id);
        return ResponseEntity.ok(
                (feedback != null) ? feedback : "AI 피드백이 아직 생성되지 않았습니다."
        );
    }

    // ====================== 좋아요/스크랩 ======================
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("좋아요를 누르려면 로그인이 필요합니다.");
        }
        postService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/scrap")
    public ResponseEntity<Void> toggleScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("스크랩을 하려면 로그인이 필요합니다.");
        }
        postService.toggleScrap(postId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // ====================== private 헬퍼 메서드들 ======================

    private String mapOrderToSortKey(String order) {
        if (order == null) return "latest";
        return switch (order.toLowerCase()) {
            case "popular"  -> "views";
            case "feedback" -> "feedback";
            default         -> "latest";   // recent 등 나머지
        };
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 50);
    }
}