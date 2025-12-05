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
	 * SSE 구독 엔드포인트
	 *
	 * - JWT 인증 정보(@CurrentUser)로 유저 ID를 가져옴
	 * - 해당 유저의 알림 스트림 연결
	 */
	@GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@CurrentUser UserContext userContext) {
		return sseService.subscribe(userContext.getUser().getId());
	}
}
