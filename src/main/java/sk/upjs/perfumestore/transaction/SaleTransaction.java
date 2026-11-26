package sk.upjs.perfumestore.transaction;


import lombok.With;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@With
public record SaleTransaction(
        Long id,
        Long customerId,
        Long employeeId,
        TransactionType transactionType,
        Long deliveryAddressId,
        Timestamp createdAt,
        OrderStatus status,
        BigDecimal totalPrice,
        List<TransactionItem> items
) {
    public enum TransactionType{
        ONLINE_ORDER,
        IN_STORE_SALE
    }
    public enum OrderStatus{
        PENDING,
        COMPLETED,
        CANCELLED
    }
}
