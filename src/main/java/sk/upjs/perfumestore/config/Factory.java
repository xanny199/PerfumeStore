package sk.upjs.perfumestore.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import sk.upjs.perfumestore.product.FragranceNoteDAO;
import sk.upjs.perfumestore.product.JdbcProductDAO;
import sk.upjs.perfumestore.product.ProductDAO;
import sk.upjs.perfumestore.product.productImage.JdbcProductImageDAO;
import sk.upjs.perfumestore.product.productImage.ProductImageDAO;
import sk.upjs.perfumestore.supply.PriceHistoryDAO;
import sk.upjs.perfumestore.supply.StockSupplyDAO;
import sk.upjs.perfumestore.transaction.JdbcSaleTransactionDAO;
import sk.upjs.perfumestore.transaction.SaleTransactionDAO;
import sk.upjs.perfumestore.user.DeliveryAddressDAO;
import sk.upjs.perfumestore.user.JdbcDeliveryAddressDAO;
import sk.upjs.perfumestore.user.JdbcUserAccountDAO;
import sk.upjs.perfumestore.user.UserAccountDAO;


public enum Factory {
    INSTANCE;

    private UserAccountDAO userAccountDao;

    private DeliveryAddressDAO deliveryAddressDao;

    private ProductDAO productDao;

    private ProductImageDAO productImageDao;

    private FragranceNoteDAO fragranceNoteDao;

    private PriceHistoryDAO priceHistoryDao;

    private StockSupplyDAO stockSupplyDao;

    private SaleTransactionDAO saleTransactionDao;

    private final HikariDataSource dataSource = createDataSource();

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    private static HikariDataSource createDataSource() {
        var pool = new HikariDataSource();

        pool.setJdbcUrl(System.getProperty("DB_URL", "jdbc:postgresql://localhost:5433/perfumestore"));
        pool.setUsername(System.getProperty("DB_USER", "perfumestore"));
        pool.setPassword(System.getProperty("DB_PASSWORD", "secretPassword"));

        return pool;
    }

    public void close() {
        dataSource.close();
    }

    public UserAccountDAO getUserAccountDao() {
        if (userAccountDao == null) {
            userAccountDao =  new JdbcUserAccountDAO(getJdbcTemplate());
        }
        return userAccountDao;
    }

    public ProductDAO getProductDao() {
        if (productDao == null) {
            productDao =  new JdbcProductDAO(getJdbcTemplate());
        }
        return productDao;
    }

    public ProductImageDAO getProductImageDao() {
        if (productImageDao == null) {
            productImageDao =  new JdbcProductImageDAO(getJdbcTemplate());
        }
        return productImageDao;
    }

    public DeliveryAddressDAO getDeliveryAddressDao() {
        if (deliveryAddressDao == null) {
            deliveryAddressDao = new JdbcDeliveryAddressDAO(getJdbcTemplate());
        }
        return deliveryAddressDao;
    }

    public SaleTransactionDAO getSaleTransactionDao() {
        if (saleTransactionDao == null) {
            saleTransactionDao = new JdbcSaleTransactionDAO(getJdbcTemplate());
        }

        return saleTransactionDao;
    }
}
