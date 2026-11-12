package sk.upjs.perfumestore.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class JdbcDeliveryAddressDAO implements DeliveryAddressDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DeliveryAddress> rowMapper = new RowMapper<DeliveryAddress>() {

        @Override
        public DeliveryAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DeliveryAddress(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("street"),
                    rs.getString("city"),
                    rs.getString("postal_code"),
                    rs.getString("country")
            );
        }
    };

    public JdbcDeliveryAddressDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DeliveryAddress> findAll() {
        String sql = """
                SELECT\s
                    id,
                    user_id,
                    street,
                    city,
                    postal_code,
                    country
                FROM delivery_address
               \s""";

        return Collections.unmodifiableList(jdbcTemplate.query(sql, rowMapper));
    }

    @Override
    public Optional<DeliveryAddress> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Delivery address id cannot be null or zero or negative");
        }

        String sql = """
                SELECT\s
                    id,
                    user_id,
                    street,
                    city,
                    postal_code,
                    country
                FROM delivery_address
                WHERE id=?
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper, id);

        if (resultSQL.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSQL.getFirst());
        }
    }

    @Override
    public List<DeliveryAddress> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Delivery address userId cannot be null or zero or negative");
        }

        String sql = """
                SELECT\s
                    id,
                    user_id,
                    street,
                    city,
                    postal_code,
                    country
                FROM delivery_address
                WHERE user_id=?
               \s""";

        return Collections.unmodifiableList(jdbcTemplate.query(sql, rowMapper, userId));
    }

    @Override
    public DeliveryAddress create(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }

        if (deliveryAddress.id() != null) {
            throw new IllegalArgumentException("Created delivery address cannot have id");
        }

        validateDeliveryAddressData(deliveryAddress);

        String sql = """
                INSERT INTO delivery_address\s
                    (user_id, street, city, postal_code, country)
                VALUES (?, ?, ?, ?, ?)
               \s""";

        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, deliveryAddress.userId());
            ps.setString(2, deliveryAddress.street());
            ps.setString(3, deliveryAddress.city());
            ps.setString(4, deliveryAddress.postalCode());
            ps.setString(5, deliveryAddress.country());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return this.findById(id).orElseThrow();
    }

    @Override
    public void update(DeliveryAddress deliveryAddress) {
        validateDeliveryAddressID(deliveryAddress);
        validateDeliveryAddressData(deliveryAddress);

        String sql = """
                UPDATE delivery_address
                SET street=?, city=?, postal_code=?, country=?
                WHERE id=?
                """;

        var affectedRows = jdbcTemplate.update(
                sql,
                deliveryAddress.street(),
                deliveryAddress.city(),
                deliveryAddress.postalCode(),
                deliveryAddress.country(),
                deliveryAddress.id()
        );

        if (affectedRows == 0) throw new IllegalArgumentException("Delivery address was not found");
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Delivery address id cannot be null or zero or negative");
        }

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM delivery_address
                    WHERE id=?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public void delete(DeliveryAddress deliveryAddress) {
        validateDeliveryAddressID(deliveryAddress);

        String sql = """
                DELETE FROM delivery_address
                WHERE id=?
                """;

        var affectedRows = jdbcTemplate.update(sql, deliveryAddress.id());
        if (affectedRows == 0) throw new IllegalArgumentException("Delivery address was not found");
    }

    private void validateDeliveryAddressID(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }

        if (deliveryAddress.id() == null || deliveryAddress.id() <= 0) {
            throw new IllegalArgumentException("Delivery address id must be greater than zero");
        }
    }

    private void validateDeliveryAddressData(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }

        if (deliveryAddress.userId() == null || deliveryAddress.userId() <= 0) {
            throw new IllegalArgumentException("Delivery address userId cannot be null or zero or negative");
        }

        if (deliveryAddress.street() == null || deliveryAddress.street().isBlank()) {
            throw new IllegalArgumentException("Delivery address street cannot be null or blank");
        }

        if (deliveryAddress.city() == null || deliveryAddress.city().isBlank()) {
            throw new IllegalArgumentException("Delivery address city cannot be null or blank");
        }

        if (deliveryAddress.postalCode() == null || deliveryAddress.postalCode().isBlank()) {
            throw new IllegalArgumentException("Delivery address postal code cannot be null or blank");
        }

        if (deliveryAddress.country() == null || deliveryAddress.country().isBlank()) {
            throw new IllegalArgumentException("Delivery address country cannot be null or blank");
        }
    }
}
