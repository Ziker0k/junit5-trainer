package com.ziker0k.validator;

import com.ziker0k.dto.CreateUserDto;
import com.ziker0k.entity.Gender;
import com.ziker0k.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserValidatorTest {

    private final CreateUserValidator createUserValidator = CreateUserValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("2000-01-01")
                .role(Role.USER.name())
                .gender(Gender.MALE.name())
                .build();

        ValidationResult actualResult = createUserValidator.validate(dto);

        assertThat(actualResult.isValid()).isTrue();
    }

    @Test
    void invalidBirthday() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("2000-01-01 12:23")
                .role(Role.USER.name())
                .gender(Gender.MALE.name())
                .build();

        ValidationResult actualResult = createUserValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo("invalid.birthday");
    }

    @Test
    void invalidGender() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("2000-01-01")
                .role(Role.USER.name())
                .gender("fake gender")
                .build();

        ValidationResult actualResult = createUserValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo("invalid.gender");
    }

    @Test
    void invalidRole() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("2000-01-01")
                .role("fake role")
                .gender(Gender.MALE.name())
                .build();

        ValidationResult actualResult = createUserValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo("invalid.role");
    }

    @Test
    void invalidRoleGenderBirthday() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("01-01-2002")
                .role("fake role")
                .gender("fake gender")
                .build();

        ValidationResult actualResult = createUserValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(3);
        List<String> errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();
        assertThat(errorCodes).contains("invalid.role", "invalid.gender", "invalid.birthday");
    }
}