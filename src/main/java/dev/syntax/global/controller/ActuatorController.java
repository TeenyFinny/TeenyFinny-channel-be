package dev.syntax.global.controller;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>ActuatorController</b> — 런타임 중 레디니스(Readiness) 상태를
 * 수동으로 전환하기 위한 내부 관리용 엔드포인트를 제공합니다.
 *
 * <p>스케일 인/드레이닝 시점에 외부 트래픽을 안전하게 차단하거나
 * 다시 수락하도록 {@link AvailabilityChangeEvent}를 발행합니다.
 * 이 컨트롤러의 엔드포인트는 일반 클라이언트에 노출하지 말고,
 * 인프라(오토스케일러/배포 파이프라인)에서만 호출하도록 보호하세요
 * (예: 내부 네트워크 한정, 방화벽/인증 적용 등).</p>
 *
 * <p><b>관련 컴포넌트:</b> {@code DrainFilter}, {@code Readiness/Drain} 필터 등이
 * {@link ReadinessState}를 참고하여 트래픽을 차단/허용합니다.</p>
 *
 * @since 1.0
 */
@RestController
public class ActuatorController {

    private final ApplicationEventPublisher publisher;

    public ActuatorController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 레디니스 상태를 {@link ReadinessState#REFUSING_TRAFFIC} 으로 전환합니다.
     * <p>이후 등록된 필터/헬스체크는 설정에 따라 새로운 요청을 503 등으로 거부할 수 있습니다.</p>
     *
     * <p><b>예시(curl)</b></p>
     * <pre>{@code
     * curl -X POST http://localhost:8080/internal/readiness/off
     * }</pre>
     */
    @PostMapping("/internal/readiness/off")
    public void off() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.REFUSING_TRAFFIC);
    }

    /**
     * 레디니스 상태를 {@link ReadinessState#ACCEPTING_TRAFFIC} 으로 전환합니다.
     * <p>드레이닝이 끝난 뒤 트래픽 수락을 재개할 때 호출합니다.</p>
     *
     * <p><b>예시(curl)</b></p>
     * <pre>{@code
     * curl -X POST http://localhost:8080/internal/readiness/on
     * }</pre>
     */
    @PostMapping("/internal/readiness/on")
    public void on() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.ACCEPTING_TRAFFIC);
    }
}
