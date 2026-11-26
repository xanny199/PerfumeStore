package sk.upjs.perfumestore.cart;

import java.util.Optional;

public interface CartDAO {

    Optional<Cart> findByUserId(Long userId);

    Cart create(Long userId);

    void addItem(Long cartId, Long productId, Integer quantity);

    void updateQuantity(Long cartId, Long productId, Integer quantity);

    void deleteItem(Long cartId, Long productId);

    void removeItem(Long cartId, Long productId);

    void clearAll(Long cartId);
}