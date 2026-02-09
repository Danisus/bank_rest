package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferRequestDto {
    @NotNull
    Long fromCardId;
    @NotNull
    Long toCardId;
    @NotNull
    @Positive
    BigDecimal amount;
}
