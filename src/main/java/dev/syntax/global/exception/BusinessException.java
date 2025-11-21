package dev.syntax.global.exception;

import dev.syntax.global.response.error.ErrorCode;
import lombok.Getter;

/**
 * 서비스·도메인 계층에서 발생하는 비즈니스 예외를 표현하는 클래스입니다.
 *
 * BusinessException은 도메인 규칙 위반, 데이터 검증 실패, 존재하지 않는 엔티티 조회 등
 * 애플리케이션 로직에서 처리해야 하는 예외 상황을 나타낼 때 사용합니다.
 * 사용 예:
 *
 * // 서비스/도메인 계층
 * if (user == null) {
 *     throw new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
 * }
 *
 * 이 클래스를 사용함으로써 서비스 로직에서 발생하는 모든 오류 상황을
 * 일관된 오류 코드(ErrorCode 기반)로 전달할 수 있으며,
 * 컨트롤러와의 책임을 명확히 분리할 수 있습니다.
 */
@Getter
public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
