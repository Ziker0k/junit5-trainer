package com.ziker0k.service;

import com.ziker0k.dao.SubscriptionDao;
import com.ziker0k.dto.CreateSubscriptionDto;
import com.ziker0k.entity.Provider;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import com.ziker0k.exception.SubscriptionException;
import com.ziker0k.exception.ValidationException;
import com.ziker0k.mapper.CreateSubscriptionMapper;
import com.ziker0k.validator.CreateSubscriptionValidator;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionDao subscriptionDao;
    private final CreateSubscriptionMapper createSubscriptionMapper;
    private final CreateSubscriptionValidator createSubscriptionValidator;
    private final Clock clock;

    public Subscription upsert(CreateSubscriptionDto dto) {
        var validationResult = createSubscriptionValidator.validate(dto);
        if (validationResult.hasErrors()) {
            throw new ValidationException(validationResult.getErrors());
        }

        Subscription subscription = subscriptionDao.findByUserId(dto.getUserId()).stream()
                .filter(existingSubscription -> existingSubscription.getName().equals(dto.getName()))
                .filter(existingSubscription -> existingSubscription.getProvider() == Provider.findByName(dto.getProvider()))
                .findFirst()
                .map(existingSubscription -> existingSubscription
                        .setExpirationDate(dto.getExpirationDate())
                        .setStatus(Status.ACTIVE))
                .orElseGet(() -> createSubscriptionMapper.map(dto));

        return subscriptionDao.upsert(subscription);
    }

    public void cancel(Integer subscriptionId) {
        var subscription = subscriptionDao.findById(subscriptionId)
                .orElseThrow(IllegalArgumentException::new);
        if (subscription.getStatus() != Status.ACTIVE) {
            throw new SubscriptionException(String.format("Only active subscription %d can be canceled", subscriptionId));
        }
        subscription.setStatus(Status.CANCELED);
        subscriptionDao.update(subscription);
    }

    public void expire(Integer subscriptionId) {
        var subscription = subscriptionDao.findById(subscriptionId)
                .orElseThrow(IllegalArgumentException::new);
        if (subscription.getStatus() == Status.EXPIRED) {
            throw new SubscriptionException(String.format("Subscription %d has already expired", subscriptionId));
        }
        subscription.setStatus(Status.EXPIRED);
        subscription.setExpirationDate(Instant.now(clock));
        subscriptionDao.update(subscription);
    }
}
