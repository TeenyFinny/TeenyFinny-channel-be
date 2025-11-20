package dev.syntax.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityUnauthorizedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * JWT 없이 인증이 필요한 API에 접근할 때
     * 401 Unauthorized를 반환하는지 테스트합니다.
     */
    @Test
    @DisplayName("JWT 없이 인증 필요 API 접근 → 401 Unauthorized")
    void unauthorizedWithoutJwt() throws Exception {

        BaseResponse<?> expected = BaseResponse.of(ErrorAuthCode.UNAUTHORIZED);
        String expectedJson = objectMapper.writeValueAsString(expected);

        mockMvc.perform(get("/test/secure")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(expectedJson));
    }
}
