package sk.upjs.transaction;


import lombok.With;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@With
public record SaleTransaction(
        Long id,
        Long customerId,
        Long employeeId,
        TransactionType transactionType,
        Long deliveryAddressId,
        OffsetDateTime createdAt,
        OrderStatus status,
        BigDecimal totalPrice
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
