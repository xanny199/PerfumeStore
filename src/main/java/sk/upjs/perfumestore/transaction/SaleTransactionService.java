package sk.upjs.perfumestore.transaction;

import org.springframework.transaction.annotation.Transactional;
import sk.upjs.perfumestore.exception.BadRequestException;
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

    @Transactional
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

    public List<SaleTransaction> getAllOnlineOrders() {
        return saleTransactionDao.findAll().stream().filter(
                order -> order.transactionType().equals(SaleTransaction.TransactionType.ONLINE_ORDER)
        ).toList();
    }

    @Transactional
    public boolean changeStatusForOrder(SaleTransaction saleTransaction, SaleTransaction.OrderStatus orderStatus) {
        if (saleTransaction == null || orderStatus == null) {
            throw new IllegalArgumentException("Invalid saleTransaction");
        }

        if (orderStatus.equals(SaleTransaction.OrderStatus.PENDING)) {
            return false;
        }

        if (!saleTransaction.transactionType().equals(SaleTransaction.TransactionType.ONLINE_ORDER)) {
            throw new IllegalArgumentException("You cannot change the status if the SaleTransaction is not of type ONLINE_ORDER.");
        }

        if (!saleTransaction.status().equals(SaleTransaction.OrderStatus.PENDING)) {
            throw new IllegalArgumentException("To change its status, a SaleTransaction must have the status PENDING.");
        }

        saleTransactionDao.changeStatus(saleTransaction.id(), orderStatus);
        return saleTransactionDao.findById(saleTransaction.id()).status().equals(SaleTransaction.OrderStatus.COMPLETED)
                || saleTransactionDao.findById(saleTransaction.id()).status().equals(SaleTransaction.OrderStatus.CANCELLED);
    }
}
