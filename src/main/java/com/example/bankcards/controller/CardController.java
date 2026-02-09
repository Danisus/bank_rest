package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    @PostMapping("/{userId}")
    public CardResponseDto createCard(@PathVariable Long userId){
        return cardService.createCard(userId);
    }

    @GetMapping
    public Page<CardResponseDto> getAllCards(Pageable pageable){
        return cardService.findAll(pageable);
    }

    @GetMapping("/{userId}")
    public Page<CardResponseDto> getCards(@PathVariable Long userId, Pageable pageable){
        return cardService.findByUserId(userId, pageable);
    }

    @PatchMapping("/block/{cardId}")
    public CardResponseDto blockCard(@PathVariable Long cardId){
        return cardService.blockCard(cardId);
    }

    @PatchMapping("/active/{cardId}")
    public CardResponseDto activateCard(@PathVariable Long cardId){
        return cardService.activateCard(cardId);
    }

    @PostMapping("/transfer")
    public List<CardResponseDto> transfer(@RequestBody @Valid TransferRequestDto transferRequestDto){
        return cardService.transfer(transferRequestDto);
    }

    @DeleteMapping("/{cardId}")
    public void deleteCard(@PathVariable Long cardId){
        cardService.deleteCard(cardId);
    }

}
