package com.ziker0k.service;

import com.ziker0k.dao.SubscriptionDao;
import com.ziker0k.dto.CreateSubscriptionDto;
import com.ziker0k.entity.Provider;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import com.ziker0k.exception.SubscriptionException;
import com.ziker0k.exception.ValidationException;
import com.ziker0k.mapper.CreateSubscriptionMapper;
import com.ziker0k.validator.CreateSubscriptionValidator;
import com.ziker0k.validator.Error;
import com.ziker0k.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class SubscriptionServiceTest {

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2024-03-04T12:00:00Z"), ZoneId.of("UTC"));
    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void upsertSuccessfullyIfSubscriptionExists() {
        Instant oldExpirationDate = Instant.now(clock).minus(30, ChronoUnit.DAYS);
        Instant newExpirationDate = Instant.now(clock).plus(30, ChronoUnit.DAYS);
        Subscription existingSubscription = createSubscription(oldExpirationDate, Status.EXPIRED);
        CreateSubscriptionDto createSubscriptionDto = createDto(newExpirationDate);
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(List.of(existingSubscription)).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        when(subscriptionDao.upsert(any(Subscription.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        Subscription expectedResult = createSubscription(newExpirationDate, Status.ACTIVE);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void upsertSuccessfullyIfSubscriptionDoesNotExist() {
        Instant newExpirationDate = Instant.now(clock).plus(30, ChronoUnit.DAYS);
        CreateSubscriptionDto createSubscriptionDto = createDto(newExpirationDate);
        Subscription subscriptionMappedFromDto = Subscription.builder()
                .userId(createSubscriptionDto.getUserId())
                .name(createSubscriptionDto.getName())
                .provider(Provider.valueOf(createSubscriptionDto.getProvider()))
                .expirationDate(createSubscriptionDto.getExpirationDate())
                .status(Status.ACTIVE)
                .build();
        Subscription savedSubscription = Subscription.builder()
                .id(1)
                .userId(subscriptionMappedFromDto.getUserId())
                .name(subscriptionMappedFromDto.getName())
                .provider(subscriptionMappedFromDto.getProvider())
                .expirationDate(subscriptionMappedFromDto.getExpirationDate())
                .status(subscriptionMappedFromDto.getStatus())
                .build();
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(Collections.emptyList()).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscriptionMappedFromDto).when(createSubscriptionMapper).map(createSubscriptionDto);
        doReturn(savedSubscription).when(subscriptionDao).upsert(subscriptionMappedFromDto);

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        Subscription expectedResult = createSubscription(newExpirationDate, Status.ACTIVE);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void upsertShouldThrowExceptionIfDtoIsInvalid() {
        CreateSubscriptionDto invalidDto = CreateSubscriptionDto.builder().build();
        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "some error"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(invalidDto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(invalidDto));
        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
        verify(subscriptionDao, never()).upsert(any(Subscription.class));
        verify(createSubscriptionMapper, never()).map(invalidDto);
    }

    @Test
    void cancelSuccessfully() {
        Instant expirationDate = Instant.now(clock).plus(60, ChronoUnit.DAYS);
        Subscription subscription = createSubscription(expirationDate, Status.ACTIVE);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        when(subscriptionDao.update(any(Subscription.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        subscriptionService.cancel(1);

        ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionDao).update(subscriptionArgumentCaptor.capture());
        Subscription actualSubscription = subscriptionArgumentCaptor.getValue();
        assertThat(actualSubscription.getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void cancelShouldThrowExceptionWhenSubscriptionNotFound() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(957649256));
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void cancelShouldThrowExceptionWhenSubscriptionStatusIsInvalid() {
        Instant expirationDate = Instant.now(clock);
        Subscription subscription = createSubscription(expirationDate, Status.EXPIRED);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());

        assertThatThrownBy(() -> subscriptionService.cancel(subscription.getId()))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining(String.format("Only active subscription %d can be canceled", subscription.getId()));
        assertThat(subscription.getStatus()).isEqualTo(Status.EXPIRED);
        verifyNoMoreInteractions(subscriptionDao);
        verify(subscriptionDao, never()).update(any(Subscription.class));
    }

    @Test
    void expireSuccessfully() {
        Instant expirationDate = Instant.now(clock).plus(60, ChronoUnit.DAYS);
        Subscription subscription = createSubscription(expirationDate, Status.ACTIVE);
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        when(subscriptionDao.update(any(Subscription.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        subscriptionService.expire(subscription.getId());
        ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionDao).update(subscriptionArgumentCaptor.capture());
        Subscription actualSubscription = subscriptionArgumentCaptor.getValue();

        assertThat(actualSubscription.getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(actualSubscription.getExpirationDate()).isEqualTo(Instant.now(clock));
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionNotFound() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(937457439));
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionStatusIsInvalid() {
        Instant expirationDate = Instant.now(clock).plus(60, ChronoUnit.DAYS);
        Subscription actualSubscription = createSubscription(expirationDate, Status.EXPIRED);
        doReturn(Optional.of(actualSubscription)).when(subscriptionDao).findById(actualSubscription.getId());

        assertThatThrownBy(() -> subscriptionService.expire(actualSubscription.getId()))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining(String.format("Subscription %d has already expired", actualSubscription.getId()));
        verifyNoMoreInteractions(subscriptionDao);
    }

    private static Subscription createSubscription(Instant expirationDate, Status status) {
        return Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(status)
                .build();
    }

    private static CreateSubscriptionDto createDto(Instant expirationDate) {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(expirationDate)
                .build();
    }
}