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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private Clock clock = Clock.systemUTC();
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
        Instant oldExpirationDate = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant newExpirationDate = Instant.now().plus(30, ChronoUnit.DAYS);
        Subscription existingSubscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(oldExpirationDate)
                .status(Status.EXPIRED)
                .build();
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(newExpirationDate)
                .build();
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(List.of((existingSubscription))).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        doReturn(existingSubscription).when(subscriptionDao).upsert(existingSubscription);

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        Subscription expectedResult = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(newExpirationDate)
                .status(Status.ACTIVE)
                .build();
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void upsertSuccessfullyIfSubscriptionDoesNotExist() {
        Instant newExpirationDate = Instant.now().plus(30, ChronoUnit.DAYS);
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(newExpirationDate)
                .build();
        Subscription subscriptionMappedFromDto = Subscription.builder()
                .userId(createSubscriptionDto.getUserId())
                .name(createSubscriptionDto.getName())
                .provider(Provider.valueOf(createSubscriptionDto.getProvider()))
                .expirationDate(createSubscriptionDto.getExpirationDate())
                .status(Status.ACTIVE)
                .build();
        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(Collections.emptyList()).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscriptionMappedFromDto).when(createSubscriptionMapper).map(createSubscriptionDto);
        doReturn(subscriptionMappedFromDto.setId(1)).when(subscriptionDao).upsert(subscriptionMappedFromDto);

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        Subscription expectedResult = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(newExpirationDate)
                .status(Status.ACTIVE)
                .build();
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void upsertShouldThrowExceptionIfDtoIsInvalid() {
        CreateSubscriptionDto invalidDto = CreateSubscriptionDto.builder()
                .build();
        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "some error"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(invalidDto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(invalidDto));
        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
    }

    @Test
    void cancelSuccessfully() {
        Instant expirationDate = Instant.now().plus(60, ChronoUnit.DAYS);
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(Status.ACTIVE)
                .build();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        doReturn(subscription).when(subscriptionDao).update(subscription);

        subscriptionService.cancel(1);

        verify(subscriptionDao, times(1)).update(subscription);
        assertThat(subscription.getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void cancelShouldThrowExceptionWhenSubscriptionNotFound() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(1));
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void cancelShouldThrowExceptionWhenSubscriptionStatusIsInvalid() {
        Instant expirationDate = Instant.now();
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(Status.EXPIRED)
                .build();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());

        assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(1));
        assertThat(subscription.getStatus()).isEqualTo(Status.EXPIRED);
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void expireSuccessfully() {
        Instant expirationDate = Instant.now().plus(60, ChronoUnit.DAYS);
        Instant now = Instant.now(Clock.systemUTC());
        Subscription subscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(Status.CANCELED)
                .build();
        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(anyInt());
        doReturn(now).when(clock).instant();

        subscriptionService.expire(1);

        assertThat(subscription.getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(subscription.getExpirationDate()).isEqualTo(now);
        verify(subscriptionDao, times(1)).update(subscription);
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionNotFound() {
        doReturn(Optional.empty()).when(subscriptionDao).findById(anyInt());

        assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(1));
        verifyNoMoreInteractions(subscriptionDao);
    }

    @Test
    void expireShouldThrowExceptionWhenSubscriptionStatusIsInvalid() {
        Instant expirationDate = Instant.now().plus(60, ChronoUnit.DAYS);
        Subscription actualSubscription = Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(Status.EXPIRED)
                .build();
        doReturn(Optional.of(actualSubscription)).when(subscriptionDao).findById(anyInt());

        assertThrows(SubscriptionException.class, () -> subscriptionService.expire(1));
        assertThat(actualSubscription.getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(actualSubscription.getExpirationDate()).isEqualTo(expirationDate);
        verifyNoMoreInteractions(subscriptionDao);
    }
}