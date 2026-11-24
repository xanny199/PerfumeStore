package sk.upjs.perfumestore.product.productImage;

import java.util.Optional;

public class ProductImageService {

    private final ProductImageDAO productImageDAO;

    public ProductImageService(ProductImageDAO productImageDAO) {
        this.productImageDAO = productImageDAO;
    }

    public Optional<ProductImage> getProductImageByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }
        return productImageDAO.findByProductId(productId);
    }

    public ProductImage save(Long productId, String imagePath) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }

        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalArgumentException("Image path cannot be empty");
        }

        String cleanPath = imagePath.trim();

        if (!cleanPath.startsWith("/sk/upjs/perfumestore/images/")) {
            throw new IllegalArgumentException("Invalid image path");
        }

        if (ProductImageService.class.getResource(cleanPath) == null) {
            throw new IllegalArgumentException("Image does not exist in resources");
        }

        return productImageDAO.save(productId, cleanPath);
    }

    public void deleteByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }
        productImageDAO.deleteByProductId(productId);
    }
}
