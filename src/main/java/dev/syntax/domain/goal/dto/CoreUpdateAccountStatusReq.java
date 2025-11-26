package dev.syntax.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoreUpdateAccountStatusReq {
    private String status;
}
