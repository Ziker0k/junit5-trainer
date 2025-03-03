package com.ziker0k.validator;

import com.ziker0k.dto.CreateSubscriptionDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator createSubscriptionValidator = CreateSubscriptionValidator.getInstance();

    private static Stream<Arguments> getWrongExpirationDate() {
        return Stream.of(
                Arguments.of(Instant.now().minus(60, ChronoUnit.DAYS))
        );
    }

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.now().plus(60, ChronoUnit.DAYS))
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        assertFalse(validationResult.hasErrors());
    }

    @Test
    void failIfUserIdIsInvalid() {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.now().plus(60, ChronoUnit.DAYS))
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        Assertions.assertThat(validationResult.getErrors()).hasSize(1);
        Assertions.assertThat(validationResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void failIfNameIsInvalid() {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name(" ")
                .provider("GOOGLE")
                .expirationDate(Instant.now().plus(60, ChronoUnit.DAYS))
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        Assertions.assertThat(validationResult.getErrors()).hasSize(1);
        Assertions.assertThat(validationResult.getErrors().get(0).getCode()).isEqualTo(101);
    }

    @Test
    void failIfProviderIsInvalid() {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("DUMMY")
                .expirationDate(Instant.now().plus(60, ChronoUnit.DAYS))
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        Assertions.assertThat(validationResult.getErrors()).hasSize(1);
        Assertions.assertThat(validationResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getWrongExpirationDate")
    void failIfExpirationDateIsInvalid(Instant expirationDate) {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(expirationDate)
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        Assertions.assertThat(validationResult.getErrors()).hasSize(1);
        Assertions.assertThat(validationResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getWrongExpirationDate")
    void failIfAllFieldsAreInvalid(Instant wrongExpirationDate) {
        CreateSubscriptionDto createSubscriptionDto = CreateSubscriptionDto.builder()
                .name(" ")
                .provider("DUMMY")
                .expirationDate(wrongExpirationDate)
                .build();

        ValidationResult validationResult = createSubscriptionValidator.validate(createSubscriptionDto);

        Assertions.assertThat(validationResult.getErrors()).hasSize(4);
        Assertions.assertThat(validationResult.getErrors().stream().map(Error::getCode)).contains(100, 101, 102, 103);
    }
}