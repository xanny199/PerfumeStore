package sk.upjs.perfumestore.transaction;

import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.*;

public class JdbcSaleTransactionDAO implements SaleTransactionDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSaleTransactionDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final ResultSetExtractor<List<SaleTransaction>> extractor = rs -> {
        Map<Long, SaleTransaction> map = new LinkedHashMap<>();
        while (rs.next()) {
            long transactionId = rs.getLong("t_id");
            if (!map.containsKey(transactionId)) {
                List<TransactionItem> items = new ArrayList<>();
                SaleTransaction transaction = new SaleTransaction(
                        transactionId,
                        rs.getObject("t_customer_id", Long.class),
                        rs.getObject("t_employee_id", Long.class),
                        SaleTransaction.TransactionType.valueOf(rs.getString("t_transaction_type")),
                        rs.getObject("t_delivery_address_id", Long.class),
                        rs.getObject("t_created_at", OffsetDateTime.class),
                        SaleTransaction.OrderStatus.valueOf(rs.getString("t_status")),
                        rs.getBigDecimal("t_total_price"),
                        items
                );
                map.put(transactionId, transaction);
            }
            long itemId = rs.getLong("i_id");
            if (!rs.wasNull()) {
                TransactionItem transactionItem = new TransactionItem(
                        itemId,
                        transactionId,
                        rs.getLong("i_product_id"),
                        rs.getInt("i_quantity"),
                        rs.getBigDecimal("i_unit_price")
                );
                map.get(transactionId).items().add(transactionItem);
            }
        }
        return new ArrayList<>(map.values());
    };

    @Override
    public SaleTransaction save(SaleTransaction saleTransaction) {
        if (saleTransaction == null) {
            throw new IllegalArgumentException("SaleTransaction cannot be null");
        }
        if (saleTransaction.id() != null) {
            throw new IllegalArgumentException("New transaction cannot already have an ID");
        }
        if (saleTransaction.items() == null || saleTransaction.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot save a transaction without items");
        }
        String sql = "INSERT INTO sale_transaction(customer_id,employee_id,transaction_type,delivery_address_id,status,total_price) VALUES(?,?,?::transaction_type, ?, ?::order_status, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, saleTransaction.customerId(), Types.BIGINT);
            ps.setObject(2, saleTransaction.employeeId(), Types.BIGINT);
            ps.setObject(3, saleTransaction.transactionType().name());
            ps.setObject(4, saleTransaction.deliveryAddressId(), Types.BIGINT);
            ps.setString(5, saleTransaction.status().name());
            ps.setBigDecimal(6, saleTransaction.totalPrice());
            return ps;
        }, keyHolder);

        Number key = (Number) keyHolder.getKeys().get("id");
        OffsetDateTime createdAt = (OffsetDateTime) keyHolder.getKeys().get("created_at");

        if (saleTransaction.items() != null && !saleTransaction.items().isEmpty()) {
            String sqlForItem = "INSERT INTO transaction_item(product_id,transaction_id,quantity,unit_price) VALUES(?,?,?,?)";
            List<Object[]> args = new ArrayList<>();
            for (TransactionItem item : saleTransaction.items()) {
                args.add(new Object[]{
                        item.productId(),
                        key.longValue(),
                        item.quantity(),
                        item.unitPrice()
                });
            }
            jdbcTemplate.batchUpdate(sqlForItem, args);

        }
        return saleTransaction.withCreatedAt(createdAt).withId(key.longValue());
    }

    @Override
    public List<SaleTransaction> findAll() {
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor);
    }

    @Override
    public SaleTransaction findById(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.id = ?
                 ORDER BY t.created_at DESC, t.id
               \s""";
        List<SaleTransaction> saleTransactions = jdbcTemplate.query(sql, extractor, id);
        if (saleTransactions.isEmpty()) return null;
        return saleTransactions.getFirst();
    }

    @Override
    public List<SaleTransaction> findByCustomerId(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.customer_id = ?
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor, id);
    }

    @Override
    public List<SaleTransaction> findByEmployeeId(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.employee_id = ?
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor, id);
    }

    @Override
    public List<SaleTransaction> findByStatus(SaleTransaction.OrderStatus status) {
        if(status==null){
            throw new IllegalArgumentException("Status cannot be null");
        }
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.status = ?::order_status
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor, status.name());
    }

    @Override
    public List<SaleTransaction> findByDateRange(OffsetDateTime start, OffsetDateTime end) {
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.created_at BETWEEN ? AND ?
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor, start, end);
    }

    @Override
    public List<SaleTransaction> findByEmployeeIdAndDates(Long employeeId, OffsetDateTime start, OffsetDateTime end) {
        if(employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        String sql = """
                SELECT\s
                 t.id AS t_id,
                 t.customer_id AS t_customer_id,
                 t.employee_id AS t_employee_id,
                 t.transaction_type AS t_transaction_type,
                 t.delivery_address_id AS t_delivery_address_id,
                 t.created_at AS t_created_at,
                 t.status AS t_status,
                 t.total_price AS t_total_price,
                 i.id AS i_id,
                 i.product_id AS i_product_id,
                 i.quantity AS i_quantity,
                 i.unit_price AS i_unit_price
                 FROM sale_transaction t
                 LEFT JOIN transaction_item i ON t.id = i.transaction_id
                 WHERE t.employee_id = ? AND t.created_at BETWEEN ? AND ?
                 ORDER BY t.created_at DESC, t.id
               \s""";
        return jdbcTemplate.query(sql, extractor, employeeId, start, end);
    }

    @Override
    public boolean changeStatus(Long id, SaleTransaction.OrderStatus status) {
        if(id==null || id <= 0){
            throw new IllegalArgumentException("ID must be strictly positive");
        }
        if(status==null){
            throw new IllegalArgumentException("Status cannot be null");
        }
        String sql = "UPDATE sale_transaction SET status = ?::order_status WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, status.name(), id);
        return rowsAffected > 0;
    }
}
