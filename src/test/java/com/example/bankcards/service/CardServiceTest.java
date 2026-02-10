package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // если Unnecessary stubbing — lenient фиксит
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private CardService cardService;

    private User createTestUser(Long id) {
        User user = User.builder()
                .id(id)
                .fullName("Test User")
                .email("test@example.com")
                .password("pass")
                .role(Role.USER)
                .cards(new ArrayList<>())  // фикс NPE
                .build();
        return user;
    }

    private Card createTestCard(Long id, User user, BigDecimal balance, Status status) {
        return Card.builder()
                .id(id)
                .cardNumber("4000001234567893")
                .user(user)
                .balance(balance)
                .status(status)
                .expirationDate(LocalDate.now().plusYears(5))
                .build();
    }

    @Test
    void createCard_success() {
        Long userId = 1L;
        User user = createTestUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        lenient().when(cardRepository.existsByCardNumber(any(String.class))).thenReturn(false);  // lenient если не всегда вызывается
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponseDto dto = cardService.createCard(userId);

        assertNotNull(dto);
        assertEquals(Status.ACTIVE, dto.getStatus());
        assertEquals(BigDecimal.ZERO, dto.getBalance());
        assertTrue(dto.getCardNumber().startsWith("**** **** **** "));
        verify(cardRepository).save(any(Card.class));
        assertTrue(user.getCards().size() == 1);  // проверка add вместо verify
    }

    @Test
    void deposit_success() {
        Long cardId = 1L;
        User user = createTestUser(1L);
        Card card = createTestCard(cardId, user, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal amount = new BigDecimal("500.00");
        CardResponseDto dto = cardService.deposit(cardId, amount);

        assertEquals(new BigDecimal("500.00"), dto.getBalance());
        verify(cardRepository).save(card);
    }

    @Test
    void deposit_notOwner_throws() {
        Long cardId = 1L;
        User owner = createTestUser(1L);
        User current = createTestUser(2L);
        Card card = createTestCard(cardId, owner, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(securityUtil.getCurrentUserId()).thenReturn(2L);

        assertThrows(RuntimeException.class, () -> cardService.deposit(cardId, new BigDecimal("100")));
    }

    @Test
    void blockCard_success() {
        Long cardId = 1L;
        User user = createTestUser(1L);
        Card card = createTestCard(cardId, user, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponseDto dto = cardService.blockCard(cardId);

        assertEquals(Status.BLOCKED, dto.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_success() {
        Long cardId = 1L;
        User user = createTestUser(1L);
        Card card = createTestCard(cardId, user, BigDecimal.ZERO, Status.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponseDto dto = cardService.activateCard(cardId);

        assertEquals(Status.ACTIVE, dto.getStatus());
    }

    @Test
    void transfer_success() {
        Long currentUserId = 1L;
        User user = createTestUser(currentUserId);
        Card fromCard = createTestCard(1L, user, new BigDecimal("1000"), Status.ACTIVE);
        Card toCard = createTestCard(2L, user, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityUtil.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequestDto dto = new TransferRequestDto(1L, 2L, new BigDecimal("500"));
        List<CardResponseDto> result = cardService.transfer(dto);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("500"), result.get(0).getBalance());
        assertEquals(new BigDecimal("500"), result.get(1).getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_insufficientFunds_throws() {
        Long currentUserId = 1L;
        User user = createTestUser(currentUserId);
        Card fromCard = createTestCard(1L, user, new BigDecimal("100"), Status.ACTIVE);
        Card toCard = createTestCard(2L, user, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityUtil.getCurrentUserId()).thenReturn(currentUserId);

        TransferRequestDto dto = new TransferRequestDto(1L, 2L, new BigDecimal("200"));

        assertThrows(RuntimeException.class, () -> cardService.transfer(dto));
    }

    @Test
    void transfer_differentOwner_throws() {
        Long currentUserId = 1L;
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        Card fromCard = createTestCard(1L, user1, new BigDecimal("1000"), Status.ACTIVE);
        Card toCard = createTestCard(2L, user2, BigDecimal.ZERO, Status.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityUtil.getCurrentUserId()).thenReturn(currentUserId);

        TransferRequestDto dto = new TransferRequestDto(1L, 2L, new BigDecimal("500"));

        assertThrows(RuntimeException.class, () -> cardService.transfer(dto));
    }
}