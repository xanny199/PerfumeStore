package sk.upjs.perfumestore.user;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressDao {
    List<DeliveryAddress> findAll();

    Optional<DeliveryAddress> findById(Long id);

    List<DeliveryAddress> findByUserId(Long userId);

    DeliveryAddress create(DeliveryAddress deliveryAddress);

    void update(DeliveryAddress deliveryAddress);

    boolean existsById(Long id);

    void delete(DeliveryAddress deliveryAddress);
}
