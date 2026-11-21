package sk.upjs.perfumestore.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import sk.upjs.perfumestore.product.FragranceNoteDAO;
import sk.upjs.perfumestore.product.JdbcProductDAO;
import sk.upjs.perfumestore.product.ProductDAO;
import sk.upjs.perfumestore.supply.PriceHistoryDAO;
import sk.upjs.perfumestore.supply.StockSupplyDAO;
import sk.upjs.perfumestore.transaction.JdbcSaleTransactionDAO;
import sk.upjs.perfumestore.transaction.SaleTransaction;
import sk.upjs.perfumestore.transaction.SaleTransactionDAO;
import sk.upjs.perfumestore.user.DeliveryAddressDAO;
import sk.upjs.perfumestore.user.JdbcDeliveryAddressDAO;
import sk.upjs.perfumestore.user.JdbcUserAccountDAO;
import sk.upjs.perfumestore.user.UserAccountDAO;

import java.time.OffsetDateTime;
import java.util.List;

public enum Factory {
    INSTANCE;

    private UserAccountDAO userAccountDao;

    private DeliveryAddressDAO deliveryAddressDao;

    private ProductDAO productDao;

    private FragranceNoteDAO fragranceNoteDao;

    private PriceHistoryDAO priceHistoryDao;

    private StockSupplyDAO stockSupplyDao;

    private SaleTransactionDAO saleTransactionDao;

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }

        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(System.getProperty("DB_URL","jdbc:postgresql://localhost:5433/perfumestore"));
        dataSource.setUser(System.getProperty("DB_USER", "postgres"));
        dataSource.setPassword(System.getProperty("DB_PASSWORD", "secretpassword"));

        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
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
