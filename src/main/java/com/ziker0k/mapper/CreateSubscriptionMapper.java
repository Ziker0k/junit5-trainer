package com.ziker0k.mapper;

import com.ziker0k.dto.CreateSubscriptionDto;
import com.ziker0k.entity.Provider;
import com.ziker0k.entity.Status;
import com.ziker0k.entity.Subscription;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CreateSubscriptionMapper implements Mapper<CreateSubscriptionDto, Subscription> {

    private static final CreateSubscriptionMapper INSTANCE = new CreateSubscriptionMapper();

    public static CreateSubscriptionMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Subscription map(CreateSubscriptionDto object) {
        return Subscription.builder()
                .userId(object.getUserId())
                .name(object.getName())
                .provider(Provider.findByNameOpt(object.getProvider()).orElse(null))
                .expirationDate(object.getExpirationDate())
                .status(Status.ACTIVE)
                .build();
    }
}
