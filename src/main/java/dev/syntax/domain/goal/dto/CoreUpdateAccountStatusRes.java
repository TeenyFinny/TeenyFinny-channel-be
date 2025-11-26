package dev.syntax.domain.goal.dto;

import lombok.Getter;

@Getter
public class CoreUpdateAccountStatusRes {
    private Long accountId;
    private String status;
}
