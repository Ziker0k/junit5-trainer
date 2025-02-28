package com.ziker0k.mapper;

import com.ziker0k.dto.UserDto;
import com.ziker0k.entity.Gender;
import com.ziker0k.entity.Role;
import com.ziker0k.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class UserMapperTest {

    private final UserMapper userMapper = UserMapper.getInstance();

    @Test
    void map() {
        User user = User.builder()
                .id(1)
                .name("Ivan")
                .email("ivan@gmail.com")
                .password("password")
                .birthday(LocalDate.of(2000, 1, 1))
                .role(Role.USER)
                .gender(Gender.MALE)
                .build();

        UserDto actualResult = userMapper.map(user);

        UserDto expectedResult = UserDto.builder()
                .id(1)
                .name("Ivan")
                .email("ivan@gmail.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .role(Role.USER)
                .gender(Gender.MALE)
                .build();
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }

}