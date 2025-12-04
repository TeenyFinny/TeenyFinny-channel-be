package dev.syntax.domain.notification.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseService {

	// 너무 길면 커넥션 누적 → 30분로 제한
	private static final long TIMEOUT = 30 * 60 * 1000L; // 30분

	/**
	 * 한 유저가 여러 탭을 열 수 있으므로
	 * emitter를 1개가 아닌 "리스트"로 관리한다.
	 * (기존 단일 Map<Long, SseEmitter> 구조는 누수 발생)
	 */
	private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	/**
	 * SSE 구독 처리
	 */
	public SseEmitter subscribe(Long userId) {

		// timeout 설정 필수 (무한 연결 금지)
		SseEmitter emitter = new SseEmitter(TIMEOUT);

		// 같은 유저가 여러 번 접속해도 emitter가 덮어써지지 않도록 리스트로 저장
		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		// 연결 종료 시 emitter 정리 (메모리/소켓 누수 방지)
		emitter.onCompletion(() -> removeEmitter(userId, emitter));
		emitter.onTimeout(() -> removeEmitter(userId, emitter));
		emitter.onError(e -> removeEmitter(userId, emitter));

		// chrome 503 방지용 더미 connect 이벤트 전송
		try {
			emitter.send(
				SseEmitter.event()
					.name("connect")
					.data("connected")
			);
		} catch (IOException ex) {
			removeEmitter(userId, emitter);
		}

		return emitter;
	}

	/**
	 * emitter 제거 로직
	 * (여기 빠지면 CLOSE_WAIT 누적)
	 */
	private void removeEmitter(Long userId, SseEmitter emitter) {
		List<SseEmitter> list = emitters.get(userId);
		if (list != null) {
			list.remove(emitter);
		}
	}

	/**
	 * 특정 사용자에게 SSE 이벤트 push
	 */
	public void send(Long userId, String name, Object data) {

		List<SseEmitter> list = emitters.get(userId);
		if (list == null) return;

		// 전송 실패한 emitter는 모아두었다가 한꺼번에 제거
		List<SseEmitter> deadEmitters = new ArrayList<>();

		for (SseEmitter emitter : list) {
			try {
				emitter.send(
					SseEmitter.event()
						.name(name)
						.data(data)
				);
			} catch (IOException e) {
				// 소켓 끊긴 emitter → 반드시 제거 (FIN_WAIT / CLOSE_WAIT 방지)
				deadEmitters.add(emitter);
			}
		}

		list.removeAll(deadEmitters);
	}
}
