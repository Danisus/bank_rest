package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDto {
    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}