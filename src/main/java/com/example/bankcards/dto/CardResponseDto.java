package com.example.bankcards.dto;

import com.example.bankcards.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CardResponseDto {

    private Long id;
    private String cardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private Status status;
    private BigDecimal balance;

}
