package sk.upjs.perfumestore.supply;

import java.time.OffsetDateTime;
import java.util.List;

public interface PriceHistoryDAO {

    PriceHistory save(PriceHistory priceHistory);

    List<PriceHistory> findAll();

    List<PriceHistory> findByProductId(Long id);

    boolean deleteById(Long id);

    List<PriceHistory> findByProductIdAndDateRange(Long id, OffsetDateTime start, OffsetDateTime end);

    List<PriceHistory> findByDateRange(OffsetDateTime start, OffsetDateTime end);
}
