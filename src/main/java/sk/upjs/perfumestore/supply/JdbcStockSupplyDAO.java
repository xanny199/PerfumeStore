package sk.upjs.perfumestore.supply;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class JdbcStockSupplyDAO implements StockSupplyDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<StockSupply> rowMapper = new DataClassRowMapper<>(StockSupply.class);

    public JdbcStockSupplyDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public StockSupply save(StockSupply stockSupply) {
        String sql = "INSERT INTO stock_supply(product_id,supplier_name,quantity_added,supply_date,purchase_price) VALUES(?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, stockSupply.productId());
            ps.setString(2, stockSupply.supplierName());
            ps.setInt(3, stockSupply.quantityAdded());
            ps.setObject(4, stockSupply.supplyDate());
            ps.setBigDecimal(5, stockSupply.purchasePrice());
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id");
        return stockSupply.withId(key.longValue());
    }

    @Override
    public List<StockSupply> findAll() {
        String sql = """
                SELECT id,product_id,supplier_name,quantity_added,supply_date,purchase_price
                 FROM stock_supply
                 ORDER BY supply_date DESC,purchase_price
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<StockSupply> findByProductId(Long id) {
        String sql = """
                SELECT id,product_id,supplier_name,quantity_added,supply_date,purchase_price
                 FROM stock_supply
                 WHERE product_id = ?
                 ORDER BY supply_date DESC,purchase_price
                """;
        return jdbcTemplate.query(sql, rowMapper, id);
    }

    @Override
    public List<StockSupply> findByProductIdAndDateRange(Long id, LocalDate start, LocalDate end) {
        String sql = """
                SELECT id,product_id,supplier_name,quantity_added,supply_date,purchase_price
                 FROM stock_supply
                 WHERE product_id = ? AND supply_date BETWEEN ? AND ?
                 ORDER BY supply_date DESC,purchase_price
                """;
        return jdbcTemplate.query(sql, rowMapper, id, start, end);
    }

    @Override
    public List<StockSupply> findByDateRange(LocalDate start, LocalDate end) {

        String sql = """
                SELECT id,product_id,supplier_name,quantity_added,supply_date,purchase_price
                 FROM stock_supply
                 WHERE supply_date BETWEEN ? AND ?
                 ORDER BY supply_date DESC,purchase_price
                """;
        return jdbcTemplate.query(sql, rowMapper, start, end);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM stock_supply WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
}
