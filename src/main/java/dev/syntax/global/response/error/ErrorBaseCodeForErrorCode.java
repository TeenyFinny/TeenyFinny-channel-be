package dev.syntax.global.response.error;

/**
 * Auth에서만 사용되는 ErrorCode를 관리하기 위해 ErrorCode를 상속받는 인터페이스입니다.<br><br>
 * ErrorAuthCode에서 구현됩니다.<br>
 */
public interface ErrorBaseCodeForErrorCode extends ErrorCode{
    String getErrorCode();
}
