package com.zentrix.command.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommandAckRequest(
        @NotNull CommandAckStatus status,
        @Size(max = 500) String detail
) {
    public enum CommandAckStatus { COMPLETADO, ERROR }
}
