package sk.upjs.perfumestore.transaction;

import java.util.List;

public class SaleTransactionService {

    private final SaleTransactionDAO saleTransactionDao;

    public SaleTransactionService(SaleTransactionDAO saleTransactionDao) {
        this.saleTransactionDao = saleTransactionDao;
    }

    public List<SaleTransaction> getUserOrders(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }

        return saleTransactionDao.findByCustomerId(userId);
    }
}
