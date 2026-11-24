package sk.upjs.perfumestore.product.productImage;

import java.util.Optional;

public interface ProductImageDAO {

    Optional<ProductImage> findByProductId(Long productId);

    ProductImage save(Long productId, String imagePath);

    void deleteByProductId(Long productId);
}
