package dev.syntax.domain.auth.service;

import static dev.syntax.global.response.error.ErrorAuthCode.PASSWORD_MISMATCH;
import static dev.syntax.global.response.error.ErrorAuthCode.SIMPLE_PASSWORD_MISMATCH;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import dev.syntax.domain.auth.dto.SimplePasswordVerifyReq;
import dev.syntax.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.syntax.domain.auth.dto.PasswordVerifyReq;
import dev.syntax.domain.auth.dto.PasswordVerifyRes;
import dev.syntax.domain.auth.dto.IdentityVerifyReq;
import dev.syntax.domain.auth.dto.IdentityVerifyRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .password("$2a$10$encodedPassword") // BCrypt 해시 예시
                .simplePassword("$2a$10$encodedSimplePassword")
                .build();
    }

    @Test
    @DisplayName("일반 비밀번호 검증 성공")
    void passwordVerify_success() {
        PasswordVerifyReq req = new PasswordVerifyReq("pass123!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getPassword())).thenReturn(true);

        PasswordVerifyRes res = authService.verifyPassword(1L, req);

        assertThat(res.matched()).isTrue();
    }

    @Test
    @DisplayName("일반 비밀번호 검증 실패")
    void passwordVerify_failure() {
        PasswordVerifyReq req = new PasswordVerifyReq("wrongpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getPassword())).thenReturn(false);

        BusinessException exception = catchThrowableOfType(
                () -> authService.verifyPassword(1L, req),
                BusinessException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("간편 비밀번호 검증 성공")
    void simplePasswordVerify_success() {
        SimplePasswordVerifyReq req = new SimplePasswordVerifyReq("123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getSimplePassword())).thenReturn(true);

        PasswordVerifyRes res = authService.verifySimplePassword(1L, req);

        assertThat(res.matched()).isTrue();
    }

    @Test
    @DisplayName("간편 비밀번호 검증 실패")
    void simplePasswordVerify_failure() {
        SimplePasswordVerifyReq req = new SimplePasswordVerifyReq("654321");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getSimplePassword())).thenReturn(false);

        BusinessException exception = catchThrowableOfType(
                () -> authService.verifySimplePassword(1L, req),
                BusinessException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(SIMPLE_PASSWORD_MISMATCH);
    }


}
