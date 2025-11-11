package dev.syntax.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <b>DrainFilter</b> — 요청 처리 중(in-flight) 건수를 계수하여
 * 드레이닝(요청 배출) 상태 판단에 활용할 수 있게 해주는 서블릿 필터.
 *
 * 현재 처리중인 요청이 몇개인지, 서버를 닫아도 되는 상황인지에 대한 정보를 응답합니다.
 *
 * <p>다음 경로는 계수에서 제외합니다.</p>
 * <ul>
 *   <li>{@code /actuator/**}</li>
 *   <li>{@code /internal/**}</li>
 * </ul>
 *
 * <p>일반 요청은 체인 진행 전 {@link #inFlight}를 증가시키고,
 * 처리 완료 후(정상/예외 무관) 반드시 감소시키도록 {@code try/finally}로 보호합니다.</p>
 *
 * @since 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DrainFilter implements Filter {
    private final AtomicInteger inFlight;

    public DrainFilter(AtomicInteger inFlight) { this.inFlight = inFlight; }

    /**
     * 전역 in-flight 카운터 빈을 등록합니다.
     * <p>정수 0으로 시작하며, 여러 컴포넌트에서 주입 받아 공유합니다.</p>
     *
     * @return 공유 {@link AtomicInteger} 카운터
     */
    @Bean
    static AtomicInteger inFlightCounter() { return new AtomicInteger(0); }

    /**
     * 요청 경로에 따라 계수 여부를 결정하고 체인을 진행합니다.
     * <ul>
     *   <li>{@code /actuator}, {@code /internal}로 시작하면 계수하지 않고 통과</li>
     *   <li>그 외 경로는 in-flight를 증가 → 체인 진행 → 반드시 감소</li>
     * </ul>
     *
     * @param req   현재 요청
     * @param res   현재 응답
     * @param chain 다음 필터로 전달하기 위한 체인
     * @throws IOException      I/O 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String uri = (req instanceof HttpServletRequest r) ? r.getRequestURI() : "";
        if (uri.startsWith("/actuator") || uri.startsWith("/internal")) {
            chain.doFilter(req, res);
            return;
        }

        inFlight.incrementAndGet();
        try {
            chain.doFilter(req, res);
        } finally {
            inFlight.decrementAndGet();
        }
    }
}

/**
 * 비동기 실행 풀 설정.
 *
 * <p>{@link EnableAsync}와 함께 {@link ThreadPoolTaskExecutor}를 구성하여
 * 애플리케이션 내부의 비동기 작업(예: @Async 메서드)의 동시성 상태를 관찰할 수 있게 합니다.</p>
 *
 * @since 1.0
 */
@Configuration @EnableAsync
class AsyncConf {
    @Bean("appExecutor")
    ThreadPoolTaskExecutor appExecutor(){
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8); ex.setMaxPoolSize(16); ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("app-"); ex.initialize(); return ex;
    }
}

/**
 * <b>DrainEndpoint</b> — 현재 HTTP in-flight 수와 비동기 실행기의 활성 스레드 수를 노출하는
 * 커스텀 액추에이터 엔드포인트.
 *
 * <p>{@code /actuator/drain}에서 조회 가능하며 다음 정보를 반환합니다.</p>
 * <ul>
 *   <li>{@code httpInFlight}: 처리 중인 HTTP 요청 수</li>
 *   <li>{@code asyncActive}: 활성 비동기 작업(스레드) 수</li>
 *   <li>{@code drained}: 두 값이 모두 0이면 {@code true}</li>
 * </ul>
 *
 * @since 1.0
 */
@Component
@Endpoint(id = "drain")
class DrainEndpoint {
    private final AtomicInteger inFlight;
    private final ThreadPoolTaskExecutor ex; // @Async 안 쓰면 주입 제거
    DrainEndpoint(AtomicInteger inFlight, @Qualifier("appExecutor") ThreadPoolTaskExecutor ex){
        this.inFlight = inFlight; this.ex = ex;
    }
    @ReadOperation
    public Map<String,Object> status(){
        int http = inFlight.get();
        int async = ex.getActiveCount();
        return Map.of("httpInFlight", http, "asyncActive", async, "drained", http==0 && async==0);
    }
}
