package com.ziker0k.service;

import com.ziker0k.dao.SubscriptionDao;
import com.ziker0k.dto.CreateSubscriptionDto;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import com.ziker0k.integration.IntegrationTestBase;
import com.ziker0k.mapper.CreateSubscriptionMapper;
import com.ziker0k.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                Clock.systemUTC()
        );
    }

    @Test
    void upsertSuccessfullyWhenSubscriptionExists() {
        CreateSubscriptionDto dto = getDto(Instant.now().plus(10, ChronoUnit.DAYS));
        subscriptionService.upsert(dto);
        Instant newExpirationDate = Instant.now().plus(180, ChronoUnit.DAYS);
        CreateSubscriptionDto updatedDto = getDto(newExpirationDate);

        Subscription actualResult = subscriptionService.upsert(updatedDto);

        assertThat(actualResult.getExpirationDate()).isEqualTo(newExpirationDate);
        assertThat(actualResult.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void upsertSuccessfullyWhenSubscriptionDoesNotExist() {
        CreateSubscriptionDto createSubscriptionDto = getDto(Instant.now().plus(60, ChronoUnit.DAYS));

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertNotNull(actualResult.getId());
    }

    @Test
    void cancelSuccessfully() {
        CreateSubscriptionDto dto = getDto(Instant.now().plus(10, ChronoUnit.DAYS));
        Subscription subscription = subscriptionService.upsert(dto);

        subscriptionService.cancel(subscription.getId());
        Subscription actualResult = subscriptionDao.findById(subscription.getId()).orElse(null);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    void expireSuccessfully() {
        CreateSubscriptionDto dto = getDto(Instant.now().plus(10, ChronoUnit.DAYS));
        Subscription subscription = subscriptionService.upsert(dto);

        subscriptionService.expire(subscription.getId());
        Subscription actualResult = subscriptionDao.findById(subscription.getId()).orElse(null);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(actualResult.getExpirationDate())
                .isNotEqualTo(dto.getExpirationDate())
                .isBefore(Instant.now());
    }

    private static CreateSubscriptionDto getDto(Instant expirationDate) {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Maxim")
                .provider("GOOGLE")
                .expirationDate(expirationDate)
                .build();
    }
}