package com.ziker0k.mapper;

import com.ziker0k.dto.CreateUserDto;
import com.ziker0k.entity.Gender;
import com.ziker0k.entity.Role;
import com.ziker0k.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserMapperTest {

    private final CreateUserMapper createUserMapper = CreateUserMapper.getInstance();

    @Test
    void map() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday("2000-01-01")
                .role(Role.USER.name())
                .gender(Gender.MALE.name())
                .build();

        User actualResult = createUserMapper.map(dto);

        User expectedResult = User.builder()
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday(LocalDate.of(2000, 1, 1))
                .role(Role.USER)
                .gender(Gender.MALE)
                .build();
        assertThat(actualResult).isEqualTo(expectedResult);
    }
}