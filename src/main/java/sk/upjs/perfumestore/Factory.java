package sk.upjs.perfumestore;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import sk.upjs.perfumestore.product.FragranceNoteDAO;
import sk.upjs.perfumestore.product.ProductDAO;
import sk.upjs.perfumestore.supply.PriceHistoryDAO;
import sk.upjs.perfumestore.supply.StockSupplyDAO;
import sk.upjs.perfumestore.transaction.SaleTransactionDAO;
import sk.upjs.perfumestore.user.DeliveryAddressDAO;
import sk.upjs.perfumestore.user.JdbcUserAccountDAO;
import sk.upjs.perfumestore.user.UserAccountDAO;

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
        dataSource.setUser(System.getProperty("DB_USER"));
        dataSource.setPassword(System.getProperty("DB_PASSWORD"));

        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    public UserAccountDAO getUserAccountDao() {
        if (userAccountDao == null) {
            userAccountDao =  new JdbcUserAccountDAO(getJdbcTemplate());
        }
        return userAccountDao;
    }
}
