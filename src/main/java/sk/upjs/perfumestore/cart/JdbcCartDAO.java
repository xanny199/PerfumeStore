package sk.upjs.perfumestore.cart;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import java.util.*;

public class JdbcCartDAO implements CartDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCartDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final ResultSetExtractor<List<Cart>> extractor = rs -> {
        Map<Long, Cart> map = new LinkedHashMap<>();
        while (rs.next()) {
            Long cartId = rs.getObject("c_id", Long.class);
            if (!map.containsKey(cartId)) {
                List<CartItem> items = new ArrayList<>();
                Cart cart = new Cart(
                        cartId,
                        rs.getObject("c_user_id", Long.class),
                        items
                );
                map.put(cartId, cart);
            }

            Long cartItemId = rs.getObject("ci_id", Long.class);
            if (cartItemId != null) {
                CartItem item = new CartItem(
                        cartItemId,
                        rs.getObject("ci_cart_id", Long.class),
                        rs.getObject("ci_product_id", Long.class),
                        rs.getInt("ci_quantity")
                );
                map.get(cartId).items().add(item);
            }
        }
        return new ArrayList<>(map.values());
    };

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must by strictly positive");
        }

        String sql = """
                SELECT
                    c.id AS c_id,
                    c.user_id AS c_user_id,
                    ci.id AS ci_id,
                    ci.cart_id AS ci_cart_id,
                    ci.product_id AS ci_product_id,
                    ci.quantity AS ci_quantity
                    FROM cart c
                    LEFT JOIN cart_item ci ON c.id = ci.cart_id
                    WHERE c.user_id = ?
                """;

        List<Cart> cart = jdbcTemplate.query(sql, extractor, userId);

        if (cart == null || cart.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cart.getFirst());
    }

    @Override
    public Cart create(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User id must by strictly positive");
        }

        String sql = """
                INSERT INTO cart (user_id)
                VALUES (?)
                """;

        jdbcTemplate.update(sql, userId);

        return findByUserId(userId).orElseThrow();
    }

    @Override
    public void addItem(Long cartId, Long productId, Integer quantity) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("Cart id must by strictly positive");
        }

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product id must by strictly positive");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must by strictly positive");
        }

        String sql = """
                INSERT INTO cart_item (cart_id, product_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (cart_id, product_id)\s
                DO UPDATE
                SET quantity = cart_item.quantity + ?
               \s""";

        jdbcTemplate.update(sql, cartId, productId, quantity, quantity);
    }

    @Override
    public void updateQuantity(Long cartId, Long productId, Integer quantity) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("Cart id must by strictly positive");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product id must by strictly positive");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must by strictly positive");
        }

        String sql = """
                UPDATE cart_item
                 SET quantity = ?
                 WHERE cart_id = ?
                    AND product_id = ?;
                """;
        jdbcTemplate.update(sql, quantity, cartId, productId);
    }

    @Override
    public void deleteItem(Long cartId, Long productId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("Cart id must by strictly positive");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product id must by strictly positive");
        }

        String sql = """
                DELETE FROM cart_item WHERE cart_id = ? AND product_id = ?;
                """;
        jdbcTemplate.update(sql, cartId, productId);
    }

    @Override
    public void removeItem(Long cartId, Long productId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("Cart id must by strictly positive");
        }

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product id must by strictly positive");
        }

        String updateSql = """
                UPDATE cart_item
                SET quantity = quantity - 1
                WHERE cart_id = ?
                    AND product_id = ?
                    AND quantity > 1
                """;
        var updatedRows = jdbcTemplate.update(updateSql, cartId, productId);

        if (updatedRows == 0) {
            String deletedSql = """
                    DELETE FROM cart_item
                    WHERE cart_id = ?
                        AND product_id = ?
                        AND quantity = 1
                    """;

            jdbcTemplate.update(deletedSql, cartId, productId);
        }
    }

    @Override
    public void clearAll(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("Cart id must by strictly positive");
        }

        String sql = """
                DELETE FROM cart_item WHERE cart_id = ?
                """;

        jdbcTemplate.update(sql, cartId);
    }
}
