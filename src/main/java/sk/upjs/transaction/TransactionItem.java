package sk.upjs.transaction;

import lombok.With;
import java.math.BigDecimal;

@With
public record TransactionItem(
        Long id,
        Long transactionId,
        Long  productId,
        Integer quantity,
        BigDecimal unitPrice
) {}
