package dev.syntax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling // Redis 없이 DB로 카카오 임시 토큰을 관리하기 때문에 사용
@SpringBootApplication
public class TeenyfinnyChannelApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(TeenyfinnyChannelApplication.class, args);
    }

}
