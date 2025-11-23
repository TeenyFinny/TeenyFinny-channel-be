package dev.syntax.domain.quiz.controller;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.service.QuizService;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuizControllerTest {

    private final QuizService quizService = Mockito.mock(QuizService.class);
    private final QuizController controller = new QuizController(quizService);

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    @Test
    @DisplayName("퀴즈 진행도 조회 컨트롤러 성공")
    void getQuizProgress_success() throws Exception {
        QuizProgressRes res = QuizProgressRes.builder()
                .progressId(2L)
                .streakDays(3)
                .courseCompleted(true)
                .quizDate(15)
                .monthlyReward(false)
                .todaySolved(1)
                .coupon(0)
                .requestCompleted(false)
                .build();

        // UserContext mock 대신 실제 객체 생성 가능
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.PARENT)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        UserContext context = new UserContext(user);

        Mockito.when(quizService.getQuizProgress(any(UserContext.class))).thenReturn(res);

        mockMvc.perform(get("/quiz/progresses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressId").value(2))
                .andExpect(jsonPath("$.data.streakDays").value(3))
                .andExpect(jsonPath("$.data.courseCompleted").value(true))
                .andExpect(jsonPath("$.data.quizDate").value(15))
                .andExpect(jsonPath("$.data.monthlyReward").value(false))
                .andExpect(jsonPath("$.data.todaySolved").value(1))
                .andExpect(jsonPath("$.data.coupon").value(0))
                .andExpect(jsonPath("$.data.requestCompleted").value(false));
    }
}
