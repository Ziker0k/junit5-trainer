package com.ziker0k.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class PropertiesUtilTest {

    private static Stream<Arguments> getPropertiesKeyValue() {
        return Stream.of(
                Arguments.of("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
                Arguments.of("db.user", "sa"),
                Arguments.of("db.password", ""),
                Arguments.of("db.driver", "org.h2.Driver"),
                Arguments.of("not.exist", null)
        );
    }

    @ParameterizedTest
    @MethodSource("getPropertiesKeyValue")
    void get(String key, String expectedValue) {
        String actualValue = PropertiesUtil.get(key);

        Assertions.assertThat(actualValue).isEqualTo(expectedValue);
    }
}