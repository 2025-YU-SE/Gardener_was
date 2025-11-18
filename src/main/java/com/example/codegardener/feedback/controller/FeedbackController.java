package com.example.codegardener.feedback.controller;

import com.example.codegardener.feedback.dto.*;
import com.example.codegardener.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // ✅ React 연동을 위해 추가
public class FeedbackController {

    private final FeedbackService feedbackService;

    private String getUsername(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        return userDetails.getUsername();
    }

    /**
     * ✅ [POST] 피드백 작성
     */
    @PostMapping
    public ResponseEntity<FeedbackResponseDto> createFeedback(
            @RequestBody FeedbackRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.createFeedback(dto, getUsername(userDetails)));
    }

    /**
     * ✅ [PUT] 피드백 수정
     */
    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponseDto> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackId, dto, getUsername(userDetails)));
    }

    /**
     * ✅ [DELETE] 피드백 삭제
     */
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        feedbackService.deleteFeedback(feedbackId, getUsername(userDetails));
        return ResponseEntity.ok("피드백이 삭제되었습니다.");
    }

    /**
     * ✅ [GET] 피드백 상세조회 (라인피드백 + 댓글 포함)
     */
    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDetailResponseDto> getFeedbackDetail(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(feedbackService.getFeedbackDetail(feedbackId));
    }

    /**
     * ✅ [GET] 게시물별 피드백 목록 조회
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<FeedbackResponseDto>> getFeedbackListByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(feedbackService.getFeedbackListByPost(postId));
    }

    /**
     * ✅ [POST] 피드백 채택
     */
    @PostMapping("/{feedbackId}/adopt")
    public ResponseEntity<String> adoptFeedback(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetails userDetails // 채택을 요청한 사용자 (게시물 작성자)
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            feedbackService.adoptFeedback(feedbackId, userDetails.getUsername());
            return ResponseEntity.ok("피드백이 채택되었습니다.");
        } catch (IllegalArgumentException e) { // 피드백/게시물/사용자 못 찾음
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // 이미 채택된 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AccessDeniedException e) { // 채택 권한 없음
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) { // 그 외 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("피드백 채택 중 오류 발생");
        }
    }


    /**
     * ✅ [POST] 좋아요 토글
     */
    @PostMapping("/{feedbackId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.toggleLike(feedbackId, getUsername(userDetails)));
    }

    // ======================================================
    // 🧩 라인피드백(LineFeedback) 관련 API
    // ======================================================

    /**
     * ✅ [POST] 라인피드백 등록
     * URL: /api/feedback/{feedbackId}/line
     */
    @PostMapping("/{feedbackId}/line")
    public ResponseEntity<LineFeedbackDto> addLineFeedback(
            @PathVariable Long feedbackId,
            @RequestBody LineFeedbackDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.addLineFeedback(feedbackId, dto, getUsername(userDetails)));
    }

    /**
     * ✅ [GET] 특정 피드백에 속한 라인피드백 목록 조회
     * URL: /api/feedback/{feedbackId}/lines
     */
    @GetMapping("/{feedbackId}/lines")
    public ResponseEntity<List<LineFeedbackDto>> getLineFeedbacks(
            @PathVariable Long feedbackId
    ) {
        return ResponseEntity.ok(feedbackService.getLineFeedbacks(feedbackId));
    }

    /**
     * ✅ [PUT] 라인피드백 수정
     * URL: /api/feedback/{feedbackId}/line/{lineFeedbackId}
     */
    @PutMapping("/{feedbackId}/line/{lineFeedbackId}")
    public ResponseEntity<LineFeedbackDto> updateLineFeedback(
            @PathVariable Long feedbackId,
            @PathVariable Long lineFeedbackId,
            @RequestBody LineFeedbackDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.updateLineFeedback(feedbackId, lineFeedbackId, dto, getUsername(userDetails)));
    }

    /**
     * ✅ [DELETE] 라인피드백 삭제
     * URL: /api/feedback/{feedbackId}/line/{lineFeedbackId}
     */
    @DeleteMapping("/{feedbackId}/line/{lineFeedbackId}")
    public ResponseEntity<String> deleteLineFeedback(
            @PathVariable Long feedbackId,
            @PathVariable Long lineFeedbackId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        feedbackService.deleteLineFeedback(feedbackId, lineFeedbackId, getUsername(userDetails));
        return ResponseEntity.ok("라인피드백이 삭제되었습니다.");
    }

    // ======================================================
    // 💬 댓글(FeedbackComment) 관련 API
    // ======================================================

    /**
     * ✅ [POST] 댓글 추가
     */
    @PostMapping("/{feedbackId}/comment")
    public ResponseEntity<FeedbackCommentDto> addComment(
            @PathVariable Long feedbackId,
            @RequestBody FeedbackCommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(feedbackService.addComment(feedbackId, dto, getUsername(userDetails)));
    }

    /**
     * ✅ [GET] 댓글 목록 조회
     */
    @GetMapping("/{feedbackId}/comment")
    public ResponseEntity<List<FeedbackCommentDto>> getCommentsByFeedback(
            @PathVariable Long feedbackId
    ) {
        return ResponseEntity.ok(feedbackService.getCommentsByFeedback(feedbackId));
    }
}
