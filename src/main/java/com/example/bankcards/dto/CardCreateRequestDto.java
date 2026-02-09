package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardCreateRequestDto {

    @NotNull
    private User user;

}
