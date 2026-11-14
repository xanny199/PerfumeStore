package sk.upjs.perfumestore.product;

import java.util.List;
import java.util.Optional;


public interface FragranceNoteDAO {

    List<FragranceNote> findAll();

    Optional<FragranceNote> findById(Long id);

    FragranceNote save(FragranceNote note);

    Boolean deleteById(Long id);
}
