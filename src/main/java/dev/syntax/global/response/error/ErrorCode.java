package dev.syntax.global.response.error;

import dev.syntax.global.response.ApiCode;

/**
 * ApiCode를 상속받아 에러코드를 관리하기 위한 인터페이스입니다. <br><br>
 *
 * Auth의 ErrorCode를 따로 관리하기 위해 ErrorBaseCodeForErrorCode 인터페이스에 상속됩니다.<br>
 * Auth를 제외한 에러코드는 ErrorBaseCode에서 구현됩니다.
 */
public interface ErrorCode extends ApiCode {
}
