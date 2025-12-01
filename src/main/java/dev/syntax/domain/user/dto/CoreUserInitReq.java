package dev.syntax.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.syntax.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CoreUserInitReq(
        @NotNull
        Long channelUserId,

        @NotNull
        Role role,

        @NotBlank
        String name,

        @NotBlank
        String phoneNumber,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate birthDate
) {
}
