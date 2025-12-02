package dev.syntax.domain.notification.service;

import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE(Server-Sent Events)를 통해 사용자에게 실시간 알림을 전송하는 서비스 클래스입니다.
 *
 * <p>사용자별로 {@link SseEmitter} 인스턴스를 생성 및 관리하며,
 * 서버에서 발생한 알림 이벤트를 개별 사용자 채널로 전송합니다.
 * Emitter는 사용자 ID를 key로 하여 {@link ConcurrentHashMap} 형태로 저장됩니다.</p>
 *
 * <h3>동작 개요</h3>
 * <ul>
 *   <li>클라이언트가 subscribe() 엔드포인트를 호출하면 SSE 연결 생성</li>
 *   <li>사용자 ID 기반으로 Emitter 저장 및 연결 종료 시 자동 제거</li>
 *   <li>send() 메서드를 통해 특정 사용자에게 이벤트 push</li>
 *   <li>503 방지를 위해 초기 연결 시 더미 이벤트("connect")를 전송</li>
 * </ul>
 *
 * <h3>동시성 처리</h3>
 * <p>{@code ConcurrentHashMap}을 사용하여 여러 사용자의 이벤트 구독 및 송신이
 * 동시에 발생해도 thread-safe를 보장합니다.</p>
 */
@Service
@Slf4j
public class SseService {

	private static final long DEFAULT_TIMEOUT = 60L * 60 * 1000; // 60분

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	/**
	 * 사용자의 SSE 구독을 생성하고 Emitter를 반환합니다.
	 *
	 * <p>Emitter가 생성되면 사용자 ID 기준으로 저장되며,
	 * completion / timeout / error 이벤트 발생 시 자동으로 제거됩니다.
	 * 또한 SSE 연결 유지 및 Chrome의 503 오류 방지를 위해
	 * "connect" 라는 이름의 초기 더미 이벤트를 즉시 전송합니다.</p>
	 *
	 * @param userId SSE 알림을 받을 사용자 ID
	 * @return 사용자와의 연결을 유지하는 {@link SseEmitter}
	 * @throws BusinessException 초기 더미 이벤트 전송 실패 시 발생
	 */
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitters.put(userId, emitter);

		// emitter 제거 콜백
		emitter.onError(e -> {
			if (e instanceof AsyncRequestNotUsableException
					|| e.getCause() instanceof IOException) {
				log.info("SSE 연결 종료(userId={}): {}", userId, e.getMessage());
			} else {
				log.error("SSE emitter 에러 발생(userId={})", userId, e);
			}
			emitters.remove(userId);
		});


		try {
			emitter.send(
				SseEmitter.event()
					.name("connect")
					.data("connected")
			);
		} catch (IOException ex) {
			log.error("초기 SSE 연결 이벤트 전송 실패: userId={}", userId, ex);
			throw new BusinessException(ErrorBaseCode.SSE_CONNECT_FAIL);
		}

		return emitter;
	}

	/**
	 * 특정 사용자에게 SSE 이벤트를 전송합니다.
	 *
	 * <p>연결이 유지 중인 사용자에게만 이벤트가 전송됩니다.
	 * 전송 과정에서 IOException이 발생하면 emitter를 제거하고
	 * 로그를 남깁니다.</p>
	 *
	 * @param userId 이벤트를 전달할 사용자 ID
	 * @param name   이벤트 이름(event: {name})
	 * @param data   전송할 데이터(payload)
	 */
	public void send(Long userId, String name, Object data) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter == null) {
			log.debug("활성화된 SSE Emitter가 없음: userId={}", userId);
			return;
		}

		try {
			emitter.send(
				SseEmitter.event()
					.name(name)
					.data(data)
			);
		} catch (IOException ex) {
			log.error("SSE 이벤트 전송 실패: userId={}", userId, ex);
			emitters.remove(userId);
		}
	}
}
