package sk.upjs.perfumestore.cart;

import java.util.List;

public record Cart(
        Long id,
        Long userId,
        List<CartItem> items
) {
}
