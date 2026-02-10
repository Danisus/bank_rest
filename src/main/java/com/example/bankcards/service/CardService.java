package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final SecurityUtil securityUtil;
    @Transactional
    public CardResponseDto createCard(Long userId){
        String numberCard = "";

        do {
            String num = "400000";
            for (int i = 1; i <= 9; i++) {
                num += random.nextInt(10);
            }
            int[] arr = new int[num.length()];
            for (int i = 0; i < num.length(); i++) {
                arr[i] = num.charAt(i) - '0';
            }
            int result = 0;
            for (int i = 1; i <= num.length(); i++) {
                if (i % 2 == 1)
                    result += arr[i - 1];
                if (i % 2 == 0) {
                    int is = arr[i - 1] * 2;
                    if (is > 9)
                        result += is - 9;
                    else
                        result += is;
                }
            }
            int end = (10 - (result % 10)) % 10;
            numberCard = num + end;
        } while (cardRepository.existsByCardNumber(numberCard));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));

        Card card = Card.builder()
                .cardNumber(numberCard)
                .user(user)
                .balance(BigDecimal.ZERO)
                .status(Status.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(8))
                .build();
        cardRepository.save(card);
        user.getCards().add(card);

        return toResponseDto(card);
    }

    public Page<CardResponseDto> findAll(Pageable pageable){
        Page<Card> cards = cardRepository.findAll(pageable);
        return cards.map(this::toResponseDto);
    }

    public Page<CardResponseDto> findByUserId(Long userId,  Pageable pageable){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        Page<Card> cards = cardRepository.findByUser(user, pageable);
        return cards.map(this::toResponseDto);
    }

    public Page<CardResponseDto> findMyCards(Pageable pageable) {
        Long currentUserId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Card> cards = cardRepository.findByUser(user, pageable);
        return cards.map(this::toResponseDto);
    }

    @Transactional
    public CardResponseDto deposit(Long cardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("Card is not ACTIVE");
        }
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!card.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Not your card");
        }

        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);

        return toResponseDto(card);
    }

    @Transactional
    public CardResponseDto blockCard(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("card not found"));
        if (card.getStatus() == Status.BLOCKED)
            return toResponseDto(card);
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!card.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Not your card");
        }

        card.setStatus(Status.BLOCKED);
        cardRepository.save(card);
        return toResponseDto(card);
    }

    @Transactional
    public CardResponseDto activateCard(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("card not found"));
        if (card.getStatus() == Status.ACTIVE)
            return toResponseDto(card);
        if (card.getExpirationDate().isBefore(LocalDate.now()))
            return toResponseDto(card);
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!card.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Not your card");
        }
        card.setStatus(Status.ACTIVE);
        cardRepository.save(card);
        return toResponseDto(card);
    }

    @Transactional
    public List<CardResponseDto> transfer(TransferRequestDto requestDto){
        Card fromCard = cardRepository.findById(requestDto.getFromCardId()).orElseThrow(() -> new RuntimeException("fromCard not found"));
        Card toCard = cardRepository.findById(requestDto.getToCardId()).orElseThrow(() -> new RuntimeException("toCard not found"));
        if (fromCard.getStatus() != Status.ACTIVE)
            throw new RuntimeException("fromCard is not ACTIVE");
        if (toCard.getStatus() != Status.ACTIVE)
            throw new RuntimeException("toCard is not ACTIVE");
        if (fromCard.getBalance().compareTo(requestDto.getAmount()) == -1)
            throw new RuntimeException("there are insufficient funds in the account");
        if (fromCard.getUser().getId() != toCard.getUser().getId())
            throw new RuntimeException("fromCard is not the same user as toCard");
        if (fromCard.getExpirationDate().isBefore(LocalDate.now()))
            throw new RuntimeException("fromCard expired");
        if (toCard.getExpirationDate().isBefore(LocalDate.now()))
            throw new RuntimeException("toCard expired");

        Long currentUserId = securityUtil.getCurrentUserId();
        if (!fromCard.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Not your card");
        }
        if (!toCard.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Not your card");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(requestDto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(requestDto.getAmount()));

        List<CardResponseDto> cards = new ArrayList<>();
        cards.add(toResponseDto(cardRepository.save(fromCard)));
        cards.add(toResponseDto(cardRepository.save(toCard)));
        return cards;
    }

    @Transactional
    public void deleteCard(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("card not found"));
        cardRepository.delete(card);
    }


    private CardResponseDto toResponseDto(Card card){
        return CardResponseDto.builder()
                .id(card.getId())
                .cardNumber("**** **** **** " + card.getCardNumber().substring(12))
                .ownerName(card.getUser().getFullName())
                .expiryDate(card.getExpirationDate())
                .balance(card.getBalance())
                .status(card.getStatus())
                .build();
    }

}
