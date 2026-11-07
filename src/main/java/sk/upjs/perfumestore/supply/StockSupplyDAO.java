package sk.upjs.perfumestore.supply;

import java.time.LocalDate;
import java.util.List;

public interface StockSupplyDAO {

    StockSupply save(StockSupply stockSupply);

    List<StockSupply> findAll();

    List<StockSupply> findByProductId(Long id);

    List<StockSupply> findByProductIdAndDateRange(Long id, LocalDate start, LocalDate end);

    List<StockSupply> findByDateRange(LocalDate start, LocalDate end);

    boolean deleteById(Long id);
}
