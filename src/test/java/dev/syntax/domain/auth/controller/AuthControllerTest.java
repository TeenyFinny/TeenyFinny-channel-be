package dev.syntax.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.service.LoginService;
import dev.syntax.domain.auth.service.SignupService;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.exception.GlobalExceptionHandler;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;

/**
 * AuthController 통합 테스트 클래스
 * 
 * <p>MockMvc를 사용하여 HTTP 요청/응답을 시뮬레이션하고,
 * 컨트롤러의 엔드포인트가 올바르게 동작하는지 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	// Mock 객체: 실제 구현체 대신 테스트용 가짜 객체를 주입
	@Mock
	private SignupService signupService;

	@Mock
	private LoginService loginService;

	// 테스트 대상: Mock 객체들이 주입된 실제 컨트롤러 인스턴스
	@InjectMocks
	private AuthController authController;

	// HTTP 요청을 시뮬레이션하는 MockMvc 객체
	private MockMvc mockMvc;
	
	// 요청 DTO를 JSON으로 변환하기 위한 ObjectMapper
	private ObjectMapper objectMapper;

	/**
	 * 각 테스트 실행 전 초기화 작업
	 * MockMvc와 ObjectMapper를 설정합니다.
	 */
	@BeforeEach
	void setUp() {
		// AuthController를 기반으로 MockMvc 인스턴스 생성
		mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler()) // 예외 발생 시 ControllerAdvice에서 처리하지 못하고 DispatcherServlet까지 전파되는 문제 발생 해결
			.build();
		objectMapper = new ObjectMapper();
	}

    // given: 테스트 데이터 준비
    // 유효한 부모 사용자 회원가입 요청 DTO 생성
    SignupReq req = new SignupReq(
            "parent@fisa.com",    // 이메일
            "fisa123!",           // 비밀번호 (8자 이상, 특수문자 포함)
            "김엄마",               // 이름
            Role.PARENT,          // 역할: 부모
            "123456",             // 간편 비밀번호 (6자리)
            "20000101",           // 생년월일 (yyyyMMdd)
            2,                    // 성별 (1: 남, 2: 여)
            "01012345678"         // 전화번호 (10~11자리)
    );

	/**
	 * TC-AUTH-001: 부모 사용자 회원가입 성공 테스트
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>유효한 부모 사용자 정보로 POST /auth/signup 요청</li>
	 *   <li>HTTP 상태 코드 201 Created 응답 확인</li>
	 *   <li>응답 본문에 data 필드가 없음을 확인 (성공 시 데이터 반환 없음)</li>
	 *   <li>SignupService.signup() 메서드가 정확히 1번 호출되었는지 검증</li>
	 * </ul>
	 */
	@Test
	@DisplayName("부모 사용자 회원가입 성공 - 201 Created")
	void signupParentSuccess() throws Exception {

		// SignupService의 signup 메서드가 호출되어도 아무 동작 하지 않도록 설정
		// (실제 DB 저장 없이 테스트)
		doNothing().when(signupService).signup(any(SignupReq.class));

		// when & then: API 호출 및 응답 검증
		mockMvc.perform(post("/auth/signup")  // POST /auth/signup 요청
						.contentType(MediaType.APPLICATION_JSON)  // Content-Type: application/json
						.content(objectMapper.writeValueAsString(req)))  // 요청 본문: SignupReq를 JSON으로 변환
				.andExpect(status().isCreated())  // HTTP 상태 코드 201 Created 검증
				.andExpect(jsonPath("$.data").doesNotExist());  // 응답 본문에 data 필드가 없음을 검증

		// verify: Mock 객체의 메서드 호출 검증
		// SignupService.signup()이 SignupReq 타입의 인자와 함께 정확히 1번 호출되었는지 확인
		verify(signupService, times(1)).signup(any(SignupReq.class));
	}


	/**
	 * TC-AUTH-003: 이메일 중복 검증 테스트
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>이미 존재하는 이메일로 POST /auth/signup 요청</li>
	 *   <li>SignupService에서 EMAIL_CONFLICT 예외 발생</li>
	 *   <li>HTTP 상태 코드 409 Conflict 응답 확인</li>
	 *   <li>응답 본문에 errorCode와 message가 올바르게 포함되어 있는지 검증</li>
	 *   <li>SignupService.signup() 메서드가 정확히 1번 호출되었는지 검증</li>
	 * </ul>
	 */
	@Test
	@DisplayName("회원가입 실패 - 중복된 이메일")
	void signupExsitingEmailFailure() throws Exception {
		// given: Mock 설정 - 이메일 중복 시나리오
		// SignupService.signup() 호출 시 EMAIL_CONFLICT 예외를 발생시키도록 설정
		// 이는 이미 존재하는 이메일로 회원가입을 시도하는 상황을 시뮬레이션
		doThrow(new BusinessException(ErrorAuthCode.EMAIL_CONFLICT))
			.when(signupService).signup(any(SignupReq.class));

		// when & then: API 호출 및 에러 응답 검증
		mockMvc.perform(post("/auth/signup")  // POST /auth/signup 요청
						.contentType(MediaType.APPLICATION_JSON)  // Content-Type: application/json
						.content(objectMapper.writeValueAsString(req)))  // 요청 본문: SignupReq를 JSON으로 변환
				.andExpect(status().isConflict())  // HTTP 상태 코드 409 Conflict 검증
				.andExpect(jsonPath("$.errorCode").value(ErrorAuthCode.EMAIL_CONFLICT.getErrorCode()))  // 에러 코드 검증
				.andExpect(jsonPath("$.message").value(ErrorAuthCode.EMAIL_CONFLICT.getMessage()));  // 에러 메시지 검증

		// verify: Mock 객체의 메서드 호출 검증
		// SignupService.signup()이 SignupReq 타입의 인자와 함께 정확히 1번 호출되었는지 확인
		// 예외가 발생했더라도 메서드는 호출되어야 함
		verify(signupService, times(1)).signup(any(SignupReq.class));
	}

	/**
	 * TC-AUTH-10: 로그인 실패 - 잘못된 비밀번호 입력
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>잘못된 비밀번호로 POST /auth/login 요청</li>
	 *   <li>LoginService에서 BadCredentialsException 발생</li>
	 *   <li>HTTP 상태 코드 401 Unauthorized 응답 확인</li>
	 *   <li>응답 본문에 errorCode와 message가 올바르게 포함되어 있는지 검증</li>
	 *   <li>LoginService.login() 메서드가 정확히 1번 호출되었는지 검증</li>
	 * </ul>
	 */
	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호 입력 시 401 Unauthorized 반환")
	void login_failure_invalid_password_returns_401() throws Exception {
		// given: 잘못된 비밀번호로 로그인 요청 DTO 생성
		LoginReq request = new LoginReq("test@teenyfinny.io", "wrong-password");
		
		// LoginService.login() 호출 시 BadCredentialsException을 발생시키도록 설정
		// 이는 비밀번호가 일치하지 않는 상황을 시뮬레이션
		when(loginService.login(any(LoginReq.class)))
			.thenThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));
		
		// when & then: API 호출 및 에러 응답 검증
		mockMvc.perform(post("/auth/login")  // POST /auth/login 요청
					.contentType(MediaType.APPLICATION_JSON)  // Content-Type: application/json
					.content(objectMapper.writeValueAsString(request)))  // 요청 본문: LoginReq를 JSON으로 변환
				.andExpect(status().isUnauthorized())  // HTTP 상태 코드 401 Unauthorized 검증
				.andExpect(jsonPath("$.errorCode").value(ErrorAuthCode.UNAUTHORIZED.getErrorCode()))  // 에러 코드 검증
				.andExpect(jsonPath("$.message").value(ErrorAuthCode.UNAUTHORIZED.getMessage()));  // 에러 메시지 검증
		
		// verify: Mock 객체의 메서드 호출 검증
		// LoginService.login()이 LoginReq 타입의 인자와 함께 정확히 1번 호출되었는지 확인
		// 예외가 발생했더라도 메서드는 호출되어야 함
		verify(loginService, times(1)).login(any(LoginReq.class));
	}

	
}
