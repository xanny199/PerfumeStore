package sk.upjs.perfumestore.transaction;

import sk.upjs.perfumestore.product.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public SaleTransaction completeInStoreSale(Long employeeId, List<Product> products, Map<Long, Integer> quantities) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Invalid employee ID");
        }

        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Sale cannot be empty");
        }

        if (quantities == null) {
            throw new IllegalArgumentException("Quantities cannot be null");
        }

        List<TransactionItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Product product : products) {

            if (product == null || product.id() == null) {
                throw new IllegalArgumentException("Invalid product");
            }

            int quantity = quantities.get(product.id());

            if (product.currentStock() == null ||
                    quantity > product.currentStock() ||
                    !product.isAvailable()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.name()
                );
            }

            TransactionItem item = new TransactionItem(
                    null,
                    null,
                    product.id(),
                    quantity,
                    product.price()
            );
            items.add(item);
            total = total.add(product.price().multiply(BigDecimal.valueOf(quantity)));
        }

        SaleTransaction sale =  new SaleTransaction(
                null,
                null,
                employeeId,
                SaleTransaction.TransactionType.IN_STORE_SALE,
                null,
                null,
                SaleTransaction.OrderStatus.COMPLETED,
                total,
                items
        );

        return saleTransactionDao.save(sale);
    }
}
