package sk.upjs.perfumestore.product.productImage;

public record ProductImage(
        Long id,
        Long productId,
        String imagePath
) {}
