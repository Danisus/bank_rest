package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.DepositRequestDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto createCard(@PathVariable Long userId) {
        return cardService.createCard(userId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        return cardService.findAll(pageable);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Page<CardResponseDto> getMyCards(Pageable pageable) {
        return cardService.findMyCards(pageable);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getUserCards(@PathVariable Long userId, Pageable pageable) {
        return cardService.findByUserId(userId, pageable);
    }

    @PostMapping("/{cardId}/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto deposit(@PathVariable Long cardId, @RequestBody @Valid DepositRequestDto dto) {
        return cardService.deposit(cardId, dto.getAmount());
    }
    
    @PatchMapping("/block/{cardId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CardResponseDto blockCard(@PathVariable Long cardId) {
        return cardService.blockCard(cardId);
    }

    @PatchMapping("/activate/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto activateCard(@PathVariable Long cardId) {
        return cardService.activateCard(cardId);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<CardResponseDto> transfer(@RequestBody @Valid TransferRequestDto dto) {
        return cardService.transfer(dto);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
    }
}