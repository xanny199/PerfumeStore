package sk.upjs.user;

import lombok.With;

@With
public record DeliveryAddress(
        Long id,
        Long userId,
        String street,
        String city,
        String postalCode,
        String country
) {}
