package dev.syntax.domain.sample;

import dev.syntax.domain.sample.controller.SampleController;
import dev.syntax.domain.sample.entity.SampleEntity;
import dev.syntax.domain.sample.repository.SampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 컨트롤러만 가볍게 올려서 엔드포인트 분기/상태코드/리포지토리 호출 여부를 검증한다.
 * ApiResponseUtil 내부 JSON 구조 변경에 둔감하게, 상태코드(status) 검증은 하지 않고
 * 대표 필드(data, message, errorCode) 유무만 확인한다.
 */
@WebMvcTest(controllers = SampleController.class)
class SampleControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ApplicationAvailability applicationAvailability;

    @MockitoBean
    SampleRepository sampleRepository;

    @Test
    @DisplayName("flag=1: 성공 상태만 응답 (data 없음)")
    void flag1_onlyStatus() throws Exception {
        mvc.perform(get("/sample/1"))
                .andExpect(status().isOk())
                // 대표 필드 유무만 확인 (예: data가 없다고 가정)
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("flag=2: 성공 상태 + 메시지/데이터 응답 (data 존재만 확인)")
    void flag2_statusWithMessage() throws Exception {
        mvc.perform(get("/sample/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("flag=3: 성공 상태 + 저장된 엔티티 DTO 응답, repository.save 호출됨")
    void flag3_statusWithDataAndRepositoryCall() throws Exception {
        // save 리턴값 스텁 (컨트롤러가 save 반환값을 DTO로 감싼다고 가정)
        SampleEntity saved = new SampleEntity();
        saved.setPrice("230,010,000"); // Utils.NumberFormattingService 결과를 가정
        given(sampleRepository.save(any(SampleEntity.class))).willReturn(saved);

        mvc.perform(get("/sample/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.price").value("230,010,000"));

        // 실제로 save가 호출됐는지, 어떤 값이 들어갔는지 대략 확인
        ArgumentCaptor<SampleEntity> captor = ArgumentCaptor.forClass(SampleEntity.class);
        verify(sampleRepository).save(captor.capture());
        assertThat(captor.getValue().getPrice()).isNotBlank(); // 포맷팅된 문자열이 세팅되어 들어간다고 가정
    }

    @Nested
    @DisplayName("실패 응답")
    class FailureCases {

        @Test
        @DisplayName("flag=4: 인증 실패 코드 응답 (errorCode 존재)")
        void flag4_unauthorized() throws Exception {
            mvc.perform(get("/sample/4"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").exists());
        }

        @Test
        @DisplayName("그 외: 잘못된 요청 (message 존재만 확인, status/errorCode는 비검증)")
        void flagElse_badRequest() throws Exception {
            mvc.perform(get("/sample/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
