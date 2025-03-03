package com.ziker0k.mapper;

import com.ziker0k.dto.CreateSubscriptionDto;
import com.ziker0k.entity.Provider;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper createSubscriptionMapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        Instant expirationDate = Instant.now().plus(60, ChronoUnit.DAYS);
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(expirationDate)
                .build();

        Subscription actualResult = createSubscriptionMapper.map(createSubscriptionDto);

        Subscription expectedResult = Subscription.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(expirationDate)
                .status(Status.ACTIVE)
                .build();
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }
}