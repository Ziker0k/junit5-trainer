package com.ziker0k.service;

import com.ziker0k.dao.UserDao;
import com.ziker0k.dto.CreateUserDto;
import com.ziker0k.dto.UserDto;
import com.ziker0k.entity.Gender;
import com.ziker0k.entity.Role;
import com.ziker0k.entity.User;
import com.ziker0k.integration.IntegrationTestBase;
import com.ziker0k.mapper.CreateUserMapper;
import com.ziker0k.mapper.UserMapper;
import com.ziker0k.validator.CreateUserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceIT extends IntegrationTestBase {

    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void init() {
        userDao = UserDao.getInstance();
        userService = new UserService(
                CreateUserValidator.getInstance(),
                userDao,
                CreateUserMapper.getInstance(),
                UserMapper.getInstance()
        );
    }

    @Test
    void login() {
        User user = userDao.save(getUser());

        Optional<UserDto> actualResult = userService.login(user.getEmail(), user.getPassword());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void create() {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto actualResult = userService.create(createUserDto);

        assertThat(actualResult.getId()).isNotNull();
    }

    private static User getUser() {
        return User.builder()
                .name("Ivan")
                .email("test@gmail.com")
                .password("password")
                .birthday(LocalDate.of(2000, 1, 1))
                .role(Role.USER)
                .gender(Gender.MALE)
                .build();
    }

    private static CreateUserDto getCreateUserDto() {
        return CreateUserDto.builder()
                .name("Brago")
                .email("brago@gmail.com")
                .password("password")
                .birthday("2000-01-01")
                .role(Role.USER.name())
                .gender(Gender.MALE.name())
                .build();
    }
}