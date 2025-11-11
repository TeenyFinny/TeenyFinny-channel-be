package dev.syntax.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 트래픽 종료(드레이닝) 상태에서 일반 요청을 503(Service Unavailable)로 차단하는 서블릿 필터.
 *
 * <p>
 * Spring Boot의 {@link ApplicationAvailability} 를 통해
 * 현재 애플리케이션의 {@link ReadinessState} 를 조회하고,
 * 상태가 {@code REFUSING_TRAFFIC} 인 경우 일반 엔드포인트 요청을 거부합니다.
 * 운영 중 배포/종료 과정에서 안전하게 트래픽을 배출(draining)할 때 사용합니다.
 * </p>
 *
 * <p><b>예외:</b> 운영 관리를 위해 {@code /actuator}로 시작하는 경로는 항상 통과시킵니다.</p>
 *
 * @since 1.0
 */

@Component
public class ReadinessFilter extends OncePerRequestFilter {
    private final ApplicationAvailability availability;
    public ReadinessFilter(ApplicationAvailability availability) { this.availability = availability; }

    /**
     * 각 요청마다 레디니스 상태를 점검하여 필요 시 요청을 차단합니다.
     *
     * <ul>
     *   <li>{@code /actuator}로 시작하는 요청은 항상 필터를 우회합니다.</li>
     *   <li>{@code ReadinessState == REFUSING_TRAFFIC} 인 경우:
     *     <ul>
     *       <li>HTTP 상태 코드를 503(Service Unavailable)으로 설정</li>
     *       <li>간단한 안내 메시지를 응답 바디에 기록</li>
     *       <li>필터 체인을 더 이상 진행하지 않고 즉시 반환</li>
     *     </ul>
     *   </li>
     *   <li>그 외에는 필터 체인을 계속 진행합니다.</li>
     * </ul>
     *
     * @param req   현재 HTTP 요청
     * @param res   현재 HTTP 응답
     * @param chain 다음 필터로 요청을 전달하기 위한 체인
     * @throws ServletException 필터 처리 중 서블릿 예외가 발생한 경우
     * @throws IOException      I/O 예외가 발생한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        // actuator는 계속 접근 가능해야 하므로 제외
        if (!uri.startsWith("/actuator")
                && availability.getReadinessState() == ReadinessState.REFUSING_TRAFFIC) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
            res.getWriter().write("Service is draining. Try again later.");
            return;
        }
        chain.doFilter(req, res);
    }
}