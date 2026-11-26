package sk.upjs.perfumestore.cart;

import java.util.Optional;

public class CartService {

    private final CartDAO cartDAO;

    public CartService(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    public Optional<Cart> findByUserId(Long userId) {
        validateUserId(userId);
        return cartDAO.findByUserId(userId);
    }


    public boolean cartExists(Long userId) {
        return findByUserId(userId).isPresent();
    }


    public Cart createCart(Long userId) {
        validateUserId(userId);
        if (cartExists(userId)) {
            throw new IllegalStateException("Cart for user " + userId + " already exists");
        }
        return cartDAO.create(userId);
    }


    public Cart getOrCreateCart(Long userId) {
        return findByUserId(userId).orElseGet(() -> cartDAO.create(userId));
    }


    public void addToCart(Long userId, Long productId, int quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product id must be strictly positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly positive");
        }

        Cart cart = getOrCreateCart(userId);
        cartDAO.addItem(cart.id(), productId, quantity);
    }

    public void updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly positive");
        }
        Cart cart = getOrCreateCart(userId);
        cartDAO.updateQuantity(cart.id(), productId, quantity);
    }

    public void deleteItem(Long userId, Long productId) {
        Cart cart = findByUserId(userId).orElseThrow(() -> new IllegalStateException("Cart didnt found"));
        cartDAO.deleteItem(cart.id(), productId);
    }

    public void clearCart(Long userId) {
        cartDAO.findByUserId(userId).ifPresent(cart -> cartDAO.clearAll(cart.id()));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must be strictly positive");
        }
    }
}
