package sk.upjs.perfumestore.product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Product save(Product product);

    Boolean deleteById(Long id);

    List<Product> findByNote(FragranceNote note);
}
