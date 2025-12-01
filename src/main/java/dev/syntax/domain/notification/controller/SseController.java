package dev.syntax.domain.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import dev.syntax.domain.notification.service.SseService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

	/**
	 * SSE(Notification) 구독을 처리하는 컨트롤러입니다.
	 *
	 * <p>클라이언트는 해당 엔드포인트에 연결하여
	 * 서버에서 발송하는 실시간 알림 이벤트를 수신할 수 있습니다.
	 * 연결은 {@link SseEmitter} 를 통해 유지되며,
	 * 서버 또는 클라이언트에서 명시적으로 닫기 전까지 지속됩니다.</p>
	 *
	 * <h3>주요 기능</h3>
	 * <ul>
	 *   <li>인증된 사용자 정보를 기반으로 SSE 채널 생성</li>
	 *   <li>사용자의 고유 ID를 사용하여 Emitter를 생성 및 관리</li>
	 *   <li>서버에서 발생하는 Notification 이벤트를 실시간 전송</li>
	 * </ul>
	 *
	 * <p><b>사용 예:</b>
	 * 프런트엔드에서는 다음과 같이 구독할 수 있습니다:
	 * <pre>
	 * const eventSource = new EventSource("/notifications/subscribe");
	 * eventSource.onmessage = (event) => console.log(event.data);
	 * </pre>
	 * </p>
	 *
	 * @param userContext 인증된 사용자 정보 (JWT 기반 UserContext)
	 * @return 클라이언트와의 SSE 연결을 유지하는 {@link SseEmitter}
	 */
    @GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@CurrentUser UserContext userContext) {
        return sseService.subscribe(userContext.getUser().getId());
    }
}
