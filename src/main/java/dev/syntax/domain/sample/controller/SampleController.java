package dev.syntax.domain.sample.controller;

import dev.syntax.domain.sample.dto.SampleDTO;
import dev.syntax.domain.sample.entity.SampleEntity;
import dev.syntax.domain.sample.repository.SampleRepository;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

/**
 * api 응답을 작성하는 방법을 볼 수 있는 샘플 컨트롤러입니다.
 */
@Controller
public class SampleController {
    private final SampleRepository sampleRepository;

    public SampleController(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @GetMapping("/sample/{flag}")
    public ResponseEntity<BaseResponse<?>> sample(@PathVariable int flag) {
        ResponseEntity<BaseResponse<?>> res;

        Logger log = LoggerFactory.getLogger("This Is Sample");

        if (flag == 1){
            // ApiResponseUtil.success에 성공 코드만 넣어주면 메시지 없이 응답 코드만 보낼 수 있습니다.
            log.info("성공 status만 응답!!");
            res = ApiResponseUtil.success(SuccessCode.OK);

        } else if (flag == 2) {
            // 원한다면 data 항목에 성공 메시지를 추가해줄 수도 있습니다.
            log.info("성공 status와 메시지 응답!");
            res = ApiResponseUtil.success(SuccessCode.OK, "성공!");

        } else if (flag == 3) {
            // ApiResponseUtil.success에 성공 코드와 DTO를 넣어주면 api 형식에 맞게 응답이 생성됩니다.
            // (JavaDoc의 예시 참고)
            LocalDateTime created = LocalDateTime.now();
            SampleEntity sample = new SampleEntity();
            sample.setPrice(Utils.NumberFormattingService(230010000));

            log.info("성공 status와 data 바디 응답!");
            res = ApiResponseUtil.success(SuccessCode.OK, SampleDTO.create(sample));

        } else if (flag == 4) {
            // ApiResponseUtil.failure에 ErrorAuthCode를 넣어주면 errorCode도 함께 응답됩니다.
            log.info("실패 status와 errorCode 응답!");
            res = ApiResponseUtil.failure(ErrorAuthCode.UNAUTHORIZED);

        } else {
            // ApiResponseUtil.failure에 ErrorBaseCode를 넣어주면 errorCode 없이 응답됩니다.
            log.info("실패 status만 응답!");
            res = ApiResponseUtil.failure(ErrorBaseCode.BAD_REQUEST);
        }

        return res;
    }
}
