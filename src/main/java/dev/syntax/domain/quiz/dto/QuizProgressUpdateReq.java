package dev.syntax.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizProgressUpdateReq {
    private Integer todaySolved;
    private Integer quizDate;
    private Boolean courseCompleted;
    private Boolean monthlyReward;
    private Integer coupon;
    private Boolean requestCompleted;
    private Integer firstQuizIdToday;
}
