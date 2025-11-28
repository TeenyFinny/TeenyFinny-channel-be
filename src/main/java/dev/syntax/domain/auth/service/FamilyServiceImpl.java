package dev.syntax.domain.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.auth.dto.OtpGenerateRes;
import dev.syntax.domain.auth.dto.OtpVerifyReq;
import dev.syntax.domain.auth.dto.OtpVerifyRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRelationshipRepository;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyServiceImpl implements FamilyService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository relationshipRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long OTP_EXPIRATION_MINUTES = 1;

    /**
     * 부모 사용자를 위한 6자리 OTP를 생성하고 UserRelationship에 저장합니다.
     * child는 null로 설정되어 나중에 자녀가 OTP를 입력하면 업데이트됩니다.
     * 이미 미사용 OTP가 있으면 삭제 후 새로운 OTP를 생성합니다.
     */
    @Override
    @Transactional
    public OtpGenerateRes generateOtp(Long userId) {
        User parent = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.ACCESS_DENIED));

        if (parent.getRole() != Role.PARENT) {
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }

        // 기존 pending OTP가 있으면 삭제 (createdAt 갱신을 위해)
        Optional<UserRelationship> existingPending = relationshipRepository.findByParentAndChildIsNull(parent);
        existingPending.ifPresent(relationshipRepository::delete);

        // 6자리 랜덤 OTP 생성
        String otp = String.format("%06d", RANDOM.nextInt(1000000));

        // 새로운 pending relationship 생성
        UserRelationship pendingRelationship = UserRelationship.builder()
                .parent(parent)
                .child(null) // 자녀는 나중에 업데이트
                .familyOtp(otp)
                .build();
        relationshipRepository.save(pendingRelationship);

        return OtpGenerateRes.builder()
                .familyOtp(otp)
                .build();
    }

    @Override
    @Transactional
    public OtpVerifyRes verifyOtpAndCreateRelationship(Long userId, OtpVerifyReq request) {
        User child = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.ACCESS_DENIED));

        if (child.getRole() != Role.CHILD) {
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }

        // 자녀가 이미 부모가 있는지 확인 (다른 부모와 등록되어 있는지)
        boolean hasParent = !child.getParents().isEmpty();

        if (hasParent) {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        }

        // OTP로 child가 null인 UserRelationship 찾기
        UserRelationship pendingRelationship = relationshipRepository
                .findByFamilyOtpAndChildIsNull(request.familyOtp())
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.FAMILY_OTP_MISMATCH));

        // 생성 시간 검증 (1분 이내)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = pendingRelationship.getCreatedAt();
        LocalDateTime expirationTime = createdAt.plusMinutes(OTP_EXPIRATION_MINUTES);

        if (now.isAfter(expirationTime)) {
            throw new BusinessException(ErrorAuthCode.FAMILY_OTP_TIMEOUT);
        }

        User parent = pendingRelationship.getParent();

        // pending relationship의 child를 업데이트
        UserRelationship updatedRelationship = UserRelationship.builder()
                .id(pendingRelationship.getId())
                .parent(parent)
                .child(child)
                .familyOtp(request.familyOtp())
                .build();
        
        relationshipRepository.save(updatedRelationship);

        return OtpVerifyRes.builder()
                .userId(child.getId())
                .parentId(parent.getId())
                .build();
    }
}
