package com.example.codegardener.post.service;

import com.example.codegardener.ai.service.AiFeedbackService;
import com.example.codegardener.post.domain.Post;
import com.example.codegardener.post.domain.PostLike;
import com.example.codegardener.post.domain.PostScrap;
import com.example.codegardener.post.dto.PostRequestDto;
import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.post.repository.PostLikeRepository;
import com.example.codegardener.post.repository.PostRepository;
import com.example.codegardener.post.repository.PostScrapRepository;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.UserProfile;
import com.example.codegardener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AiFeedbackService aiFeedbackService;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;

    // ====================== CRUD ======================

    @Transactional
    public PostResponseDto create(PostRequestDto dto, String currentUsername) {
        validateCodingTest(dto);

        User author = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다. username=" + currentUsername));

        Post p = Post.builder()
                .user(author)
                .title(dto.getTitle())
                .content(dto.getContent())
                .code(dto.getCode())
                .summary(dto.getSummary())
                .contentsType(dto.getContentsType())
                .githubRepoUrl(dto.getGithubRepoUrl())
                .problemStatement(dto.getProblemStatement())
                .langTags(normalizeCsv(dto.getLanguages()))
                .stackTags(normalizeCsv(dto.getStacks()))
                .build();

        Post saved = postRepository.save(p);

        UserProfile authorProfile = saved.getUser().getUserProfile();
        if (authorProfile != null) {
            authorProfile.setPostCount(authorProfile.getPostCount() + 1);
            log.debug("Incremented post count for user {}", author.getUserName());
        } else {
            log.warn("UserProfile not found for author {} during post creation.", author.getUserName());
        }

        log.info("[POST] saved postId={}", saved.getPostId());
        return PostResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> list() {
        return postRepository.findAll()
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }

    // ====================== 목록(페이징) 통합 메서드 ======================

    /**
     * 게시글 목록 페이징 조회
     *
     * @param page         0-based page index
     * @param size         page size (1 ~ 50)
     * @param contentsType null이면 전체, true/false 로 필터링
     * @param sortBy       "views", "feedback", 그 외는 "createdAt" 기준 최신순
     */
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostList(int page, int size, Boolean contentsType, String sortBy) {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 50);

        Sort sort = switch (safe(sortBy)) {
            case "views"    -> Sort.by(Sort.Direction.DESC, "views");
            case "feedback" -> Sort.by(Sort.Direction.DESC, "feedbackCount");
            default         -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage = (contentsType == null)
                ? postRepository.findAll(pageable)
                : postRepository.findByContentsType(contentsType, pageable);

        return postPage.map(PostResponseDto::fromEntity);
    }

    // ====================== 단건 조회 ======================

    @Transactional(readOnly = true)
    public PostResponseDto get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
        return PostResponseDto.fromEntity(p);
    }

    @Transactional
    public PostResponseDto update(Long id, PostRequestDto dto, String currentUsername) {
        User currentUser = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        Long ownerId = (p.getUser() != null) ? p.getUser().getUserId() : null;

        if (!Objects.equals(ownerId, currentUser.getUserId())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        validateCodingTest(dto);

        p.setTitle(dto.getTitle());
        p.setContent(dto.getContent());
        p.setCode(dto.getCode());
        p.setSummary(dto.getSummary());
        p.setContentsType(dto.getContentsType());
        p.setGithubRepoUrl(dto.getGithubRepoUrl());
        p.setProblemStatement(dto.getProblemStatement());
        p.setLangTags(normalizeCsv(dto.getLanguages()));
        p.setStackTags(normalizeCsv(dto.getStacks()));

        return PostResponseDto.fromEntity(p);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        User currentUser = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        Long ownerId = (p.getUser() != null) ? p.getUser().getUserId() : null;

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = Objects.equals(ownerId, currentUser.getUserId());

        if (!isOwner && !isAdmin) { // 본인도 아니고 관리자도 아니면
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        postRepository.delete(p);
    }

    // ====================== 통합 검색 ======================

    @Transactional(readOnly = true)
    public Page<PostResponseDto> discoverAdvanced(
            String q,
            List<String> languages,
            String langsCsv,
            List<String> stacks,
            String stacksCsv,
            Boolean contentsType,
            int page,
            int size,
            String sortKey
    ) {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(page, size); // 정렬은 네이티브 쿼리에서 처리

        String qLike = buildLikeParam(q);
        List<String> langList  = mergeParamsToList(languages, langsCsv);
        List<String> stackList = mergeParamsToList(stacks,    stacksCsv);

        String langRegex  = listToRegex(langList);
        String stackRegex = listToRegex(stackList);

        Page<Post> data = postRepository.discover(
                qLike,
                contentsType,
                langRegex,
                stackRegex,
                safe(sortKey),
                pageable
        );

        return data.map(PostResponseDto::fromEntity);
    }

    // ====================== AI 피드백 ======================

    @Transactional
    public PostResponseDto generateAiFeedback(Long postId, Long requesterId) {
        if (requesterId != null) {
            log.debug("[AI] generate request by userId={} for postId={}", requesterId, postId);
        }

        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        String aiText = aiFeedbackService.generateTextForPost(postId);
        p.setAiFeedback(aiText);
        log.info("[AI] Feedback generated manually for postId={}", postId);
        return PostResponseDto.fromEntity(p);
    }

    @Transactional(readOnly = true)
    public String getAiFeedback(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."))
                .getAiFeedback();
    }

    // ====================== Utils ======================

    private void validateCodingTest(PostRequestDto dto) {
        if (Boolean.FALSE.equals(dto.getContentsType())
                && (dto.getProblemStatement() == null || dto.getProblemStatement().isBlank())) {
            throw new IllegalArgumentException("코딩테스트 게시물은 problemStatement(문제 설명)가 필수입니다.");
        }
    }

    private String normalizeCsv(String csv) {
        if (csv == null || csv.isBlank()) return null;
        String normalized = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.joining(","));
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> mergeParamsToList(List<String> arrayParam, String csvParam) {
        List<String> list = new ArrayList<>();
        if (arrayParam != null) list.addAll(arrayParam);
        if (csvParam != null && !csvParam.isBlank()) {
            list.addAll(Arrays.stream(csvParam.split(",")).toList());
        }
        return list.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());
    }

    private String listToRegex(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        String body = values.stream()
                .map(this::escapeRegex)
                .collect(Collectors.joining("|"));
        return "(^|,)(" + body + ")(,|$)";
    }

    private String escapeRegex(String s) {
        return s.replaceAll("([^A-Za-z0-9_\\-])", "\\\\$1");
    }

    private String safe(String s) {
        return (s == null) ? "latest" : s.toLowerCase();
    }

    private String buildLikeParam(String raw) {
        if (raw == null) return null;
        String t = raw.trim().toLowerCase();
        if (t.isEmpty()) return null;
        t = t.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + t + "%";
    }

    // ====================== 좋아요 / 스크랩 ======================

    @Transactional
    public void toggleLike(Long postId, String currentUsername) {
        User user = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUsername));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다. ID: " + postId));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            PostLike newLike = new PostLike();
            newLike.setUser(user);
            newLike.setPost(post);
            postLikeRepository.save(newLike);
            post.setLikesCount(post.getLikesCount() + 1);
        }
    }

    @Transactional
    public void toggleScrap(Long postId, String currentUsername) {
        User user = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUsername));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다. ID: " + postId));

        Optional<PostScrap> existingScrap = postScrapRepository.findByUserAndPost(user, post);

        if (existingScrap.isPresent()) {
            postScrapRepository.delete(existingScrap.get());
            post.setScrapCount(Math.max(0, post.getScrapCount() - 1));
        } else {
            PostScrap newScrap = new PostScrap();
            newScrap.setUser(user);
            newScrap.setPost(post);
            newScrap.setCreatedAt(LocalDateTime.now());
            postScrapRepository.save(newScrap);
            post.setScrapCount(post.getScrapCount() + 1);
        }
    }

    // 마이페이지: 사용자가 스크랩한 게시물 페이징
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getScrappedPostsByUsername(String username, Pageable pageable) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<PostScrap> scraps = postScrapRepository.findAllByUser(user, pageable);

        return scraps.map(scrap -> PostResponseDto.fromEntity(scrap.getPost()));
    }

    // 마이페이지: 사용자가 최근 스크랩한 게시물 4개
    @Transactional(readOnly = true)
    public List<PostResponseDto> getRecentScrappedPostsByUsername(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<PostScrap> scraps = postScrapRepository.findFirst4ByUserOrderByCreatedAtDesc(user);

        return scraps.stream()
                .map(PostScrap::getPost)
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 마이페이지: 사용자가 등록한 게시물 페이징
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    // 마이페이지: 사용자가 최근 등록한 게시물 4개
    @Transactional(readOnly = true)
    public List<PostResponseDto> getRecentPostsByUserId(Long userId) {
        List<Post> posts = postRepository.findFirst4ByUser_UserIdOrderByCreatedAtDesc(userId);
        return posts.stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 좋아요 기준 인기 게시글 4개
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPopularPosts(Boolean contentsType) {
        List<Post> popularPosts = postRepository.findTop7ByContentsTypeOrderByLikesCountDesc(contentsType);
        return popularPosts.stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}