package sk.upjs.perfumestore.product.productImage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import sk.upjs.perfumestore.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcProductImageDAO implements ProductImageDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductImageDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProductImage> productImageRowMapper = new RowMapper<ProductImage>() {
        @Override
        public ProductImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProductImage(
                    rs.getObject("id", Long.class),
                    rs.getObject("product_id", Long.class),
                    rs.getString("image_path")
            );
        }
    };

    @Override
    public Optional<ProductImage> findByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }

        String sql = """
                SELECT
                    id,
                    product_id,
                    image_path
                FROM product_image
                WHERE product_id = ?
                """;

        List<ProductImage> productImage = jdbcTemplate.query(sql, productImageRowMapper, productId);
        if (productImage.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(productImage.getFirst());
    }

    @Override
    public ProductImage save(Long productId, String imagePath) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }

        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalArgumentException("Image path cannot be null or blank");
        }

        String sql = """
                INSERT INTO product_image (product_id, image_path)\s
                VALUES (?, ?)
                ON CONFLICT (product_id)
                DO UPDATE SET image_path = ?
               \s""";

        jdbcTemplate.update(sql, productId, imagePath.trim(), imagePath.trim());

        return findByProductId(productId).orElseThrow();
    }

    @Override
    public void deleteByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must by strictly positive");
        }

        String sql = """
                DELETE FROM product_image WHERE product_id = ?;
                """;

        jdbcTemplate.update(sql, productId);
    }
}
