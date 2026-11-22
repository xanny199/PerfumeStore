package sk.upjs.perfumestore.cart;

public record CartItem(
        Long id,
        Long cartId,
        Long productId,
        Integer quantity
) {
}
