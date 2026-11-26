package sk.upjs.perfumestore.transaction;

import java.sql.Timestamp;
import java.util.List;

public interface SaleTransactionDAO {

    SaleTransaction save(SaleTransaction saleTransaction);

    List<SaleTransaction> findAll();

    SaleTransaction findById(Long id);

    List<SaleTransaction> findByCustomerId(Long id);

    List<SaleTransaction> findByEmployeeId(Long id);

    List<SaleTransaction> findByStatus(SaleTransaction.OrderStatus status);

    List<SaleTransaction> findByDateRange(Timestamp start, Timestamp end);

    List<SaleTransaction> findByEmployeeIdAndDates(Long employeeId, Timestamp start, Timestamp end);

    boolean changeStatus(Long id, SaleTransaction.OrderStatus status);
}
