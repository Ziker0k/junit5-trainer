package com.ziker0k.dao;

import com.ziker0k.entity.Gender;
import com.ziker0k.entity.Role;
import com.ziker0k.entity.User;
import com.ziker0k.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoIT extends IntegrationTestBase {

    private final UserDao userDao = UserDao.getInstance();

    @Test
    void findAll() {
        User user1 = userDao.save(getUser("test1@gmail.com"));
        User user2 = userDao.save(getUser("test2@gmail.com"));
        User user3 = userDao.save(getUser("test3@gmail.com"));

        List<User> actualResult = userDao.findAll();

        Assertions.assertThat(actualResult).hasSize(8);
        List<Integer> userIds = actualResult.stream()
                .map(User::getId)
                .toList();
        Assertions.assertThat(userIds).contains(user1.getId(), user2.getId(), user3.getId());
    }

    @Test
    void findById() {
        User user = userDao.save(getUser("test1@gmail.com"));

        Optional<User> actualResult = userDao.findById(user.getId());

        Assertions.assertThat(actualResult).isPresent();
        Assertions.assertThat(actualResult.get()).isEqualTo(user);
    }

    @Test
    void save() {
        User user = getUser("test1@gmail.com");

        User actualResult = userDao.save(user);

        assertNotNull(actualResult.getId());
    }

    @Test
    void findByEmailAndPassword() {
        User user = userDao.save(getUser("test1@gmail.com"));

        Optional<User> actualResult = userDao.findByEmailAndPassword(user.getEmail(), user.getPassword());

        Assertions.assertThat(actualResult).isPresent();
        Assertions.assertThat(actualResult.get()).isEqualTo(user);
    }

    @Test
    void shouldNotFindByEmailAndPasswordIfUserDoesNotExist() {
        Optional<User> actualResult = userDao.findByEmailAndPassword("dummy", "wrongPassword");

        Assertions.assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteExistingUser() {
        User user = userDao.save(getUser("test1@gmail.com"));

        boolean actualResult = userDao.delete(user.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteNonExistingUser() {
        boolean actualResult = userDao.delete(100500400);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        User user = getUser("test1@gmail.com");
        userDao.save(user);
        user.setName("updated name");
        user.setPassword("updated password");

        userDao.update(user);

        Optional<User> updatedUser = userDao.findById(user.getId());
        Assertions.assertThat(updatedUser.get()).isEqualTo(user);
    }

    private static User getUser(String email) {
        return User.builder()
                .name("Ivan")
                .email(email)
                .password("password")
                .birthday(LocalDate.of(2000, 1, 1))
                .role(Role.USER)
                .gender(Gender.MALE)
                .build();
    }
}