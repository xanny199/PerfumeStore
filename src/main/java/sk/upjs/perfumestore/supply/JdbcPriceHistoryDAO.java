package sk.upjs.perfumestore.supply;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;

public class JdbcPriceHistoryDAO implements PriceHistoryDAO {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PriceHistory> rowMapper = new DataClassRowMapper<>(PriceHistory.class);

    public JdbcPriceHistoryDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PriceHistory save(PriceHistory priceHistory) {
        if(priceHistory == null){
            throw new IllegalArgumentException("priceHistory cannot be null");
        }
        if(priceHistory.id() != null){
            throw new IllegalArgumentException("New record cannot already have an ID");
        }
        if(priceHistory.newPrice().compareTo(BigDecimal.ZERO) <= 0 || priceHistory.oldPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Price of product must be greater than zero");
        }
        String sql = "INSERT INTO price_history(product_id,old_price,new_price,changed_at) VALUES(?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, priceHistory.productId());
            ps.setBigDecimal(2, priceHistory.oldPrice());
            ps.setBigDecimal(3, priceHistory.newPrice());
            ps.setObject(4, priceHistory.changedAt());
            return ps;
        }, keyHolder);

        Number key = (Number)keyHolder.getKeys().get("id");

        return priceHistory.withId(key.longValue());
    }

    @Override
    public List<PriceHistory> findAll() {
        String sql = """
                SELECT id,product_id,old_price,new_price,changed_at
                 FROM price_history
                 ORDER BY changed_at DESC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<PriceHistory> findByProductId(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                        SELECT id,product_id,old_price,new_price,changed_at
                         FROM price_history
                         WHERE product_id = ?
                         ORDER BY changed_at DESC
                """;
        return jdbcTemplate.query(sql, rowMapper, id);
    }

    @Override
    public List<PriceHistory> findByProductIdAndDateRange(Long id, OffsetDateTime start, OffsetDateTime end) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                SELECT id,product_id,old_price,new_price,changed_at
                FROM price_history
                WHERE product_id = ? AND changed_at BETWEEN ? AND ?
                ORDER BY changed_at DESC
                """;

        return jdbcTemplate.query(sql, rowMapper, id, start, end);
    }

    @Override
    public List<PriceHistory> findByDateRange(OffsetDateTime start, OffsetDateTime end) {

        String sql = """
                SELECT id,product_id,old_price,new_price,changed_at
                FROM price_history
                WHERE changed_at BETWEEN ? AND ?
                ORDER BY changed_at DESC
                """;

        return jdbcTemplate.query(sql, rowMapper, start, end);
    }

    @Override
    public boolean deleteById(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                DELETE FROM price_history
                WHERE id = ?
                """;
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
}
