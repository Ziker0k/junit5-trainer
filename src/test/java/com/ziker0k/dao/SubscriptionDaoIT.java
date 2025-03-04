package com.ziker0k.dao;

import com.ziker0k.entity.Provider;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import com.ziker0k.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    private static Subscription createSubscription(Integer userId, String name) {
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.now().plus(60, ChronoUnit.DAYS))
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    void findAll() {
        Subscription subscription1 = subscriptionDao.insert(createSubscription(1, "Ivan"));
        Subscription subscription2 = subscriptionDao.insert(createSubscription(2, "Maxim"));
        Subscription subscription3 = subscriptionDao.insert(createSubscription(3, "Egor"));

        List<Subscription> actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        List<Integer> subscriptionIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();
        assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());
    }

    @Test
    void findById() {
        Subscription subscription1 = subscriptionDao.insert(createSubscription(1, "Ivan"));

        Optional<Subscription> actualResult = subscriptionDao.findById(subscription1.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(subscription1);
    }

    @Test
    void shouldReturnEmptyWhenSubscriptionNotFound() {
        subscriptionDao.insert(createSubscription(1, "Ivan"));

        Optional<Subscription> actualResult = subscriptionDao.findById(214546573);

        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteExistingSubscription() {
        Subscription subscription1 = subscriptionDao.insert(createSubscription(1, "Ivan"));

        boolean actualResult = subscriptionDao.delete(subscription1.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteShouldReturnFalseIfSubscriptionDoesNotExist() {
        subscriptionDao.insert(createSubscription(1, "Ivan"));

        boolean actualResult = subscriptionDao.delete(123545745);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        Subscription subscription1 = createSubscription(1, "Ivan");
        subscriptionDao.insert(subscription1);
        subscription1.setName("Changed").setStatus(Status.EXPIRED);

        subscriptionDao.update(subscription1);

        Optional<Subscription> updatedSubscription = subscriptionDao.findById(subscription1.getId());
        assertThat(updatedSubscription).isPresent();
        assertThat(updatedSubscription.get()).isEqualTo(subscription1);
    }

    @Test
    void insert() {
        Subscription subscription1 = createSubscription(1, "Ivan");

        Subscription actualResult = subscriptionDao.insert(subscription1);

        assertNotNull(actualResult.getId());
    }

    @Test
    void findByUserId() {
        Subscription subscription1 = subscriptionDao.insert(createSubscription(1, "Ivan"));
        subscriptionDao.insert(createSubscription(2, "Maxim"));

        List<Subscription> actualResult = subscriptionDao.findByUserId(subscription1.getUserId());

        assertThat(actualResult).hasSize(1);
        assertThat(actualResult).contains(subscription1);
    }

    @Test
    void shouldNotFindByUserIdIfNotExists() {
        subscriptionDao.insert(createSubscription(1, "Ivan"));
        subscriptionDao.insert(createSubscription(2, "Maxim"));

        List<Subscription> actualResult = subscriptionDao.findByUserId(92392415);

        assertThat(actualResult).isEmpty();
    }
}