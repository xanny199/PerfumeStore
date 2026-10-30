package sk.upjs.product;

import java.util.List;
import java.util.Optional;


public interface FragranceNoteDAO {

    List<FragranceNote> findAll();

    Optional<FragranceNote> findById(int id);

    FragranceNote save(FragranceNote note);

    Boolean deleteById(int id);
}
