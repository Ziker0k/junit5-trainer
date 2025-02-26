package com.ziker0k.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateFormatterTest {

    @Test
    void format() {
        String date = "2025-02-21";

        LocalDate actualResult = LocalDateFormatter.format(date);

        assertThat(actualResult).isEqualTo(LocalDate.of(2025, 2, 21));
    }

    @Test
    void shouldThrowExceptionWhenDateIsInvalid() {
        String date = "2025-02-21 12:34:56";

        assertThrows(DateTimeParseException.class, () -> LocalDateFormatter.format(date));
    }

    @ParameterizedTest
    @MethodSource(value = "getValidationArguments")
    void isValid(String date, boolean expectedResult) {
        boolean actualResult = LocalDateFormatter.isValid(date);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getValidationArguments() {
        return Stream.of(
                Arguments.of("2025-02-21", true),
                Arguments.of("01-01-2001", false),
                Arguments.of("2025-02-21 12:34:56", false),
                Arguments.of(null, false)
        );
    }
}
