package sk.upjs.perfumestore.product;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class JdbcFragranceNoteDAO implements FragranceNoteDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<FragranceNote> rowMapper = new DataClassRowMapper<>(FragranceNote.class);

    public JdbcFragranceNoteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FragranceNote> findAll() {
        String sql = "SELECT id,name FROM fragrance_note";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<FragranceNote> findById(int id) {
        String sql = "SELECT id,name FROM fragrance_note WHERE id = ?";
        List<FragranceNote> fragranceNotes = jdbcTemplate.query(sql, rowMapper, id);
        if (fragranceNotes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fragranceNotes.get(0));
    }

    @Override
    public FragranceNote save(FragranceNote note) {
        String sql = "INSERT INTO fragrance_note (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection ->{
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,note.name());
            return ps;
        }, keyHolder);
        Number key = (Number) keyHolder.getKeys().get("id");

        return note.withId(key.longValue());
    }

    @Override
    public Boolean deleteById(int id) {
        String sql = "DELETE FROM fragrance_note WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
}
