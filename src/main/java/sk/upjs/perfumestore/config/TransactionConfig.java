package sk.upjs.perfumestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sk.upjs.perfumestore.transaction.SaleTransactionService;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TransactionConfig {

    @Bean
    public JdbcTransactionManager transactionManager() {
        var dataSource = Factory.INSTANCE.getJdbcTemplate().getDataSource();
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public SaleTransactionService saleTransactionService() {
        return new SaleTransactionService(Factory.INSTANCE.getSaleTransactionDao());
    }
}
