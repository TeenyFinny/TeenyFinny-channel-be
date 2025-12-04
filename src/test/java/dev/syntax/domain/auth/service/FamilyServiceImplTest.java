package dev.syntax.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.syntax.domain.auth.dto.OtpGenerateRes;
import dev.syntax.domain.auth.dto.OtpVerifyReq;
import dev.syntax.domain.auth.dto.OtpVerifyRes;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRelationshipRepository;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;

/**
 * FamilyServiceImpl 테스트 클래스
 * 
 * <p>OTP 생성 및 검증 로직을 테스트합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class FamilyServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserRelationshipRepository relationshipRepository;

	@Mock
	private OtpRateLimitService otpRateLimitService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private FamilyServiceImpl familyService;

	private User parentUser;
	private User childUser;

	@BeforeEach
	void setUp() {
		// 부모 사용자 생성
		parentUser = User.builder()
			.id(1L)
			.email("parent@teenyfinny.io")
			.password("encodedPw")
			.name("부모")
			.role(Role.PARENT)
			.build();

		// 자녀 사용자 생성
		childUser = User.builder()
			.id(2L)
			.email("child@teenyfinny.io")
			.password("encodedPw")
			.name("자녀")
			.role(Role.CHILD)
			.build();
	}

	/**
	 * TC-FAMILY-001: OTP 생성 성공
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>부모 사용자가 OTP 생성 요청</li>
	 *   <li>6자리 숫자 OTP가 생성됨</li>
	 *   <li>OTP 응답이 반환됨</li>
	 * </ul>
	 */
	@Test
	@DisplayName("OTP 생성 성공 - 6자리 OTP 생성 및 저장")
	void generate_otp_success() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
		OtpGenerateRes res = familyService.generateOtp(1L);

		assertThat(res.familyOtp()).matches("\\d{6}");
		verify(otpRateLimitService).validateAndRecordOtpRequest(1L);
		verify(relationshipRepository).save(any(UserRelationship.class));
	}

	/**
	 * TC-FAMILY-002: 자녀 OTP 검증 및 가족 연결 성공
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>자녀 사용자가 OTP 입력 (POST /auth/otp)</li>
	 *   <li>OTP로 pending UserRelationship 조회</li>
	 *   <li>UserRelationship의 child 필드 업데이트</li>
	 *   <li>가족 등록 알림 전송</li>
	 *   <li>userId와 parentId 반환 (201 Created, 가족 연결 성공)</li>
	 * </ul>
	 */
    @Test
    @DisplayName("자녀가 OTP 인증을 성공하면 가족 관계가 생성된다")
    void verifyOtp_success() {
        UserRelationship pending = UserRelationship.builder()
                .id(100L)
                .parent(parentUser)
                .child(null)
                .familyOtp("123456")
                .build();
        
        // BaseEntity의 createdAt은 @Builder로 설정할 수 없으므로 ReflectionTestUtils 사용
        ReflectionTestUtils.setField(pending, "createdAt", LocalDateTime.now());

        when(userRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(relationshipRepository.findByFamilyOtpAndChildIsNull("123456"))
                .thenReturn(Optional.of(pending));

        OtpVerifyReq req = new OtpVerifyReq("123456");

        // when
        OtpVerifyRes res = familyService.verifyOtpAndCreateRelationship(2L, req);

        // then
        assertThat(res.userId()).isEqualTo(childUser.getId());
        assertThat(res.parentId()).isEqualTo(parentUser.getId());

        verify(relationshipRepository).findByFamilyOtpAndChildIsNull("123456");
        verify(relationshipRepository).save(any(UserRelationship.class));
        
        // 알림 전송 검증 - 부모와 자녀에게 각각 다른 알림 전송
        verify(notificationService, times(1)).sendFamilyRegistrationNotice(parentUser, childUser.getName());
        verify(notificationService, times(1)).sendFamilyRegistrationChildNotice(childUser, parentUser.getName());
    }


	

}
