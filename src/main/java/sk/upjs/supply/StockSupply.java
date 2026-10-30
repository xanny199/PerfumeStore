package sk.upjs.supply;

import lombok.With;
import java.math.BigDecimal;
import java.time.LocalDate;

@With
public record StockSupply(
        Long id,
        Long productId,
        String supplierName,
        Integer quantityAdded,
        LocalDate supplyDate,
        BigDecimal purchasePrice
) {}
