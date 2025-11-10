package com.example.codegardener.feedback.service;

import com.example.codegardener.feedback.domain.*;
import com.example.codegardener.feedback.dto.*;
import com.example.codegardener.feedback.repository.*;
import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.repository.PostRepository;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.UserProfile;
import com.example.codegardener.user.repository.UserRepository;
import com.example.codegardener.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackLikesRepository feedbackLikesRepository;
    private final LineFeedbackRepository lineFeedbackRepository;
    private final FeedbackCommentRepository feedbackCommentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    private User findUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다: " + username));
    }

    // ============================================================
    // ✅ 피드백 CRUD
    // ============================================================

    // ✅ 피드백 작성
    public FeedbackResponseDto createFeedback(FeedbackRequestDto dto, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        Feedback feedback = dto.toEntity(currentUser, post);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        post.setFeedbackCount(post.getFeedbackCount() + 1);
        postRepository.save(post); // 게시물의 피드백 수 업데이트

        UserProfile authorProfile = currentUser.getUserProfile();
        if (authorProfile != null) {
            authorProfile.setTotalFeedbackCount(authorProfile.getTotalFeedbackCount() + 1);
            log.debug("Incremented total feedback count for user {}", currentUser.getUserName());
        } else {
            log.warn("UserProfile not found for author {} during feedback creation.", currentUser.getUserName());
        }

        return FeedbackResponseDto.fromEntity(savedFeedback);
    }

    // ✅ 피드백 수정 (채택된 피드백은 수정 불가)
    public FeedbackResponseDto updateFeedback(Long feedbackId, FeedbackRequestDto dto, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        if (!feedback.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException("본인만 피드백을 수정할 수 있습니다.");
        }
        if (feedback.getAdoptedTF()) {
            throw new IllegalStateException("채택된 피드백은 수정할 수 없습니다.");
        }

        feedback.setContent(dto.getContent());
        feedback.setRating(dto.getRating());
        feedback.setUpdatedAt(java.time.LocalDateTime.now());

        return FeedbackResponseDto.fromEntity(feedbackRepository.save(feedback));
    }

    // ✅ 피드백 삭제
    public void deleteFeedback(Long feedbackId, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        Post post = feedback.getPost();
        if (post == null) {
            throw new IllegalArgumentException("게시물을 찾을 수 없습니다.");
        }

        Long ownerId = feedback.getUser().getUserId();

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = Objects.equals(ownerId, currentUser.getUserId());

        if (!isOwner && !isAdmin) { // 본인도 아니고 관리자도 아니면
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        if (feedback.getAdoptedTF()) {
            throw new IllegalStateException("채택된 피드백은 삭제할 수 없습니다.");
        }

        feedbackRepository.delete(feedback);

        post.setFeedbackCount(Math.max(0, post.getFeedbackCount() - 1));
    }

    // ✅ 피드백 상세조회 (라인피드백 + 댓글 포함)
    public FeedbackDetailResponseDto getFeedbackDetail(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));
        return FeedbackDetailResponseDto.fromEntity(feedback);
    }

    // ✅ 게시물별 피드백 목록 조회
    public List<FeedbackResponseDto> getFeedbackListByPost(Long postId) {
        return feedbackRepository.findByPost_PostId(postId).stream()
                .map(FeedbackResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 좋아요 토글
    public String toggleLike(Long feedbackId, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        Optional<FeedbackLike> existingLike = feedbackLikesRepository.findByUserAndFeedback_FeedbackId(currentUser, feedbackId);

        if (existingLike.isPresent()) {
            feedbackLikesRepository.delete(existingLike.get());
            feedback.setLikesCount(feedback.getLikesCount() - 1);
            feedbackRepository.save(feedback);
            return "좋아요 취소";
        } else {
            FeedbackLike like = FeedbackLike.builder()
                    .feedback(feedback)
                    .user(currentUser)
                    .build();
            feedbackLikesRepository.save(like);
            feedback.setLikesCount(feedback.getLikesCount() + 1);
            feedbackRepository.save(feedback);
            return "좋아요 추가";
        }
    }

    // 피드백 채택
    @Transactional
    public void adoptFeedback(Long feedbackId, String postAuthorUsername) {
        // 채택 권한 확인 (게시물 작성자만 가능)
        User postAuthor = userRepository.findByUserName(postAuthorUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + postAuthorUsername));
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다: " + feedbackId));
        Post post = feedback.getPost(); // 2. 변경
        if (post == null) {
            throw new IllegalArgumentException("게시물을 찾을 수 없습니다.");
        }
        if (!post.getUser().getUserId().equals(postAuthor.getUserId())) {
            throw new AccessDeniedException("게시물 작성자만 피드백을 채택할 수 있습니다.");
        }

        // 이미 채택된 피드백인지 확인
        if (feedback.getAdoptedTF()) {
            throw new IllegalStateException("이미 채택된 피드백입니다.");
        }

        // 피드백 채택 상태 변경
        feedback.setAdoptedTF(true);

        // 피드백 작성자(User 객체) 조회
        User feedbackAuthor = feedback.getUser();

        // 피드백 작성자에게 포인트 지급
        if (feedbackAuthor != null) {
            userService.awardPointsForAdoptedFeedback(feedbackAuthor); // 포인트 지급 및 채택 카운트 증가
        } else {
            log.warn("Feedback author not found for awarding points."); // ID 대신 메시지 수정
        }

        log.info("Feedback {} adopted for post {} by user {}", feedbackId, post.getPostId(), postAuthorUsername);
    }

    // ============================================================
    // 🧩 라인피드백 CRUD
    // ============================================================

    // ✅ [CREATE] 라인피드백 등록
    public LineFeedbackDto addLineFeedback(Long feedbackId, LineFeedbackDto dto, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        LineFeedback lineFeedback = LineFeedback.builder()
                .feedback(feedback)
                .user(currentUser)
                .lineNumber(dto.getLineNumber())
                .endLineNumber(dto.getEndLineNumber())
                .content(dto.getContent())
                .build();

        return LineFeedbackDto.fromEntity(lineFeedbackRepository.save(lineFeedback));
    }

    // ✅ [READ] 특정 피드백의 라인피드백 목록 조회
    public List<LineFeedbackDto> getLineFeedbacks(Long feedbackId) {
        return lineFeedbackRepository.findByFeedback_FeedbackId(feedbackId)
                .stream()
                .map(LineFeedbackDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ [UPDATE] 라인피드백 수정
    public LineFeedbackDto updateLineFeedback(Long feedbackId, Long lineFeedbackId, LineFeedbackDto dto, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        LineFeedback lineFeedback = lineFeedbackRepository.findById(lineFeedbackId)
                .orElseThrow(() -> new IllegalArgumentException("라인피드백을 찾을 수 없습니다."));

        if (!lineFeedback.getFeedback().getFeedbackId().equals(feedbackId)) {
            throw new IllegalStateException("잘못된 피드백 ID입니다.");
        }

        if (!lineFeedback.getUser().getUserId().equals(currentUser.getUserId())) { // 2. 변경
            throw new IllegalStateException("본인만 수정할 수 있습니다.");
        }

        lineFeedback.setContent(dto.getContent());
        lineFeedback.setEndLineNumber(dto.getEndLineNumber());
        return LineFeedbackDto.fromEntity(lineFeedbackRepository.save(lineFeedback));
    }

    // ✅ [DELETE] 라인피드백 삭제
    public void deleteLineFeedback(Long feedbackId, Long lineFeedbackId, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        LineFeedback lineFeedback = lineFeedbackRepository.findById(lineFeedbackId)
                .orElseThrow(() -> new IllegalArgumentException("라인피드백을 찾을 수 없습니다."));

        if (!lineFeedback.getFeedback().getFeedbackId().equals(feedbackId)) {
            throw new IllegalStateException("잘못된 피드백 ID입니다.");
        }

        if (!lineFeedback.getUser().getUserId().equals(currentUser.getUserId())) { // 2. 변경
            throw new IllegalStateException("본인만 삭제할 수 있습니다.");
        }

        lineFeedbackRepository.delete(lineFeedback);
    }

    // ============================================================
    // 💬 댓글(Comment) CRUD
    // ============================================================

    // ✅ 댓글 작성
    public FeedbackCommentDto addComment(Long feedbackId, FeedbackCommentDto dto, String currentUsername) {
        User currentUser = findUserByUsername(currentUsername);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("피드백을 찾을 수 없습니다."));

        FeedbackComment comment = FeedbackComment.builder()
                .feedback(feedback)
                .user(currentUser)
                .content(dto.getContent())
                .build();

        return FeedbackCommentDto.fromEntity(feedbackCommentRepository.save(comment));
    }

    // ✅ 댓글 목록 조회
    public List<FeedbackCommentDto> getCommentsByFeedback(Long feedbackId) {
        return feedbackCommentRepository.findByFeedback_FeedbackId(feedbackId).stream()
                .map(FeedbackCommentDto::fromEntity)
                .collect(Collectors.toList());
    }

}
