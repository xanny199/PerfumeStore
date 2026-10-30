package sk.upjs.supply;

import lombok.With;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@With
public record PriceHistory(
        Long id,
        Long productId,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        OffsetDateTime changedAt
) {}
