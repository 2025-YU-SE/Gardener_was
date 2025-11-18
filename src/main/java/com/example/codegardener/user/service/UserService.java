package com.example.codegardener.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.codegardener.global.jwt.TokenBlacklist;
import com.example.codegardener.global.jwt.TokenBlacklistRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.codegardener.global.jwt.JwtUtil;
import com.example.codegardener.user.domain.Role;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.domain.UserProfile;
import com.example.codegardener.user.dto.LoginRequestDto;
import com.example.codegardener.user.dto.SignUpRequestDto;
import com.example.codegardener.user.dto.UserResponseDto;
import com.example.codegardener.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    private static final String GRADE_SEED = "새싹 개발자";
    private static final String GRADE_LEAF = "잎새 개발자";
    private static final String GRADE_TREE = "나무 개발자";
    private static final String GRADE_SAGE = "숲의 현자";
    private static final String GRADE_DELETED = "탈퇴한 사용자";

    // 회원 가입
    @Transactional
    public UserResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        if (userRepository.findByUserName(signUpRequestDto.getUserName()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.findByEmail(signUpRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        User newUser = new User();
        newUser.setUserName(signUpRequestDto.getUserName());
        newUser.setEmail(signUpRequestDto.getEmail());
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        newUser.setPassword(encodedPassword);
        newUser.setRole(Role.USER);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(newUser);
        userProfile.setPoints(1000);
        newUser.setUserProfile(userProfile);

        updateGrade(userProfile);

        User savedUser = userRepository.save(newUser);
        log.info("New user signed up: {} (ID: {}), initial points: 1000, grade: {}",
                savedUser.getUserName(), savedUser.getUserId(), savedUser.getUserProfile().getGrade());
        return UserResponseDto.fromEntity(savedUser);
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUserName(loginRequestDto.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return jwtUtil.createToken(user.getUserName(), user.getRole());
    }

    // 로그아웃
    @Transactional
    public void logout(String token) {
        // 토큰에서 남은 유효 시간 계산 등은 생략하고, 단순히 저장
        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setToken(token);
        blacklist.setExpiryDate(LocalDateTime.now().plusHours(1)); // 예: 1시간 뒤 만료로 설정

        tokenBlacklistRepository.save(blacklist);
    }

    // 사용자 프로필 조회 (Public)
    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
        return UserResponseDto.fromEntity(user);
    }

    private UserProfile getUserProfile(User user) {
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            log.error("UserProfile not found for user: {}", user.getUserName());
            throw new IllegalStateException("사용자 프로필 정보를 찾을 수 없습니다.");
        }
        return profile;
    }

    // 회원 탈퇴
    @Transactional
    public void deleteCurrentUser(String currentUsername) {
        User currentUser = userRepository.findByUserName(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));

        if (currentUser.getRole() == Role.ADMIN) {
            throw new IllegalStateException("관리자 계정은 스스로 탈퇴할 수 없습니다. 다른 관리자에게 요청하세요.");
        }

        log.info("User '{}' (userId={}) is deleting their own account (Soft Delete).",
                currentUser.getUserName(), currentUser.getUserId());

        anonymizeAndSoftDelete(currentUser);
    }

    // ===== 포인트 및 등급 관련 =====
    @Transactional
    public void addPoints(User user, int pointsToAdd, String reason) {
        if (pointsToAdd <= 0) {
            log.warn("Attempted to add non-positive points ({}) to user {}", pointsToAdd, user.getUserName());
            return;
        }

        UserProfile userProfile = getUserProfile(user);
        int currentPoints = userProfile.getPoints();
        userProfile.setPoints(currentPoints + pointsToAdd);

        updateGrade(userProfile); // 등급 업데이트

        log.info("Added {} points to user {} for [{}]. New points: {}, New grade: {}",
                pointsToAdd, user.getUserName(), reason, userProfile.getPoints(), userProfile.getGrade());
    }

    private void updateGrade(UserProfile userProfile) {
        int points = userProfile.getPoints();
        String newGrade;

        if (points >= 10000) {
            newGrade = GRADE_SAGE;
        } else if (points >= 5000) {
            newGrade = GRADE_TREE;
        } else if (points >= 2000) {
            newGrade = GRADE_LEAF;
        } else {
            newGrade = GRADE_SEED;
        }

        if (!newGrade.equals(userProfile.getGrade())) {
            log.info("User {}'s grade changed from {} to {}",
                    userProfile.getUser().getUserName(), userProfile.getGrade(), newGrade);
            userProfile.setGrade(newGrade);
        }
    }

    @Transactional
    public void awardPointsForAdoptedFeedback(User feedbackAuthor) {
        addPoints(feedbackAuthor, 100, "피드백 채택");
        UserProfile profile = getUserProfile(feedbackAuthor);
        profile.setAdoptedFeedbackCount(profile.getAdoptedFeedbackCount() + 1);
    }

    @Transactional
    public String recordAttendance(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        LocalDate today = LocalDate.now();
        UserProfile userProfile = getUserProfile(user);

        // 마지막 출석일 확인
        if (userProfile.getLastAttendanceDate() != null && userProfile.getLastAttendanceDate().isEqual(today)) {
            return "오늘은 이미 출석했습니다.";
        }

        // 출석 처리: 포인트 추가 및 마지막 출석 날짜 업데이트
        addPoints(user, 50, "일일 출석");
        userProfile.setLastAttendanceDate(today);

        return "출석 완료! 50 포인트가 지급되었습니다.";
    }

    // ===== 관리자 기능 =====
    @Transactional
    public void deleteUserByAdmin(Long userIdToDelete, String adminUsername) {
        User adminUser = userRepository.findByUserName(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        if (adminUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("사용자 삭제 권한이 없습니다.");
        }

        User userToDelete = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 사용자를 찾을 수 없습니다. ID: " + userIdToDelete));

        if (userToDelete.getUserId().equals(adminUser.getUserId())) {
            throw new IllegalArgumentException("자기 자신을 삭제할 수 없습니다.");
        }

        log.warn("[ADMIN] Admin '{}' is deleting user '{}' (userId={}) (Soft Delete)",
                adminUsername, userToDelete.getUserName(), userIdToDelete);

        anonymizeAndSoftDelete(userToDelete);
    }

    // 4. Soft Delete 및 익명화 처리를 위한 헬퍼 메소드
    /**
     * 사용자를 '탈퇴' 상태로 만들고 개인정보를 익명화
     * @ SQLRestriction("deleted_at IS NULL")에 의해 이후 조회 대상에서 제외
     */
    private void anonymizeAndSoftDelete(User user) {
        if (user == null) return;

        // 1. 개인 식별 정보(PII) 익명화
        String uniqueId = user.getUserId().toString();
        user.setUserName("deleted_user_" + uniqueId); // (중복 방지)
        user.setEmail(uniqueId + "@deleted.user"); // (중복 방지)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 비밀번호 스크램블

        // 2. 프로필 정보 업데이트
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            profile.setUserPicture(null); // 프로필 사진 삭제
            profile.setGrade(GRADE_DELETED); // 등급 변경
        }

        // 3. Soft Delete 플래그 설정
        user.setDeletedAt(LocalDateTime.now());

        // 4. 업데이트 저장
        userRepository.save(user);
    }
}