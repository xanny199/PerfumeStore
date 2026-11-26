package sk.upjs.perfumestore.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

public class JdbcProductDAO implements ProductDAO {

    private final JdbcTemplate jdbcTemplate;

    private final ResultSetExtractor<List<Product>> extractor = rs -> {
        Map<Long, Product> map = new LinkedHashMap<>();
        while (rs.next()) {
            Long productId = rs.getLong("product_id");
            if (!map.containsKey(productId)) {
                List<FragranceNote> notes = new ArrayList<>();
                String targetAudience = rs.getString("target_audience");
                Product product = new Product(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getString("brand"),
                        Product.CategoryType.valueOf(rs.getString("category_type")),
                        targetAudience == null ? null : Product.TargetAudience.valueOf(targetAudience),
                        rs.getString("description"),
                        rs.getInt("volume_ml"),
                        rs.getBigDecimal("price"),
                        rs.getInt("release_year"),
                        rs.getInt("current_stock"),
                        rs.getBoolean("is_available"),
                        notes
                );
                map.put(productId, product);
            }
            long noteId = rs.getLong("note_id");
            if (noteId > 0) {
                FragranceNote note = new FragranceNote(noteId, rs.getString("note_name"));
                map.get(productId).notes().add(note);
            }
        }
        return new ArrayList<>(map.values());
    };

    public JdbcProductDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Product> findAll() {
        String sql = """
                    SELECT\s
                     p.id AS product_id,
                     p.name AS product_name,
                     p.brand,
                     p.category_type,
                     p.description,
                     p.target_audience,
                     p.volume_ml,
                     p.price,
                     p.release_year,
                     p.current_stock,
                     p.is_available,
                     fn.id AS note_id,
                     fn.name AS note_name
                     FROM product p
                     LEFT JOIN product_note pn ON p.id = pn.product_id
                     LEFT JOIN fragrance_note fn ON pn.note_id = fn.id
                     ORDER BY p.id       \s
               \s""";

        return jdbcTemplate.query(sql, this.extractor);
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = """
                    SELECT\s
                     p.id AS product_id,
                     p.name AS product_name,
                     p.brand,
                     p.category_type,
                     p.description,
                     p.target_audience,
                     p.volume_ml,
                     p.price,
                     p.release_year,
                     p.current_stock,
                     p.is_available,
                     fn.id AS note_id,
                     fn.name AS note_name   \s
                     FROM product p
                     LEFT JOIN product_note pn ON p.id = pn.product_id
                     LEFT JOIN fragrance_note fn ON pn.note_id = fn.id
                     WHERE p.id = ?
               \s""";

        List<Product> products = jdbcTemplate.query(sql, this.extractor, id);

        return products.stream().findFirst();
    }

    @Override
    public Product save(Product product) {
        if(product == null) {
            throw new NullPointerException("Product cannot be null");
        }
        if(product.id() != null){
            throw new NullPointerException("New product cannot already have an ID");
        }

        String sql = "INSERT INTO product(name,brand,category_type,target_audience,description,volume_ml,price,release_year,current_stock,is_available) VALUES (?,?,CAST(? AS category_type),CAST(? AS target_audience),?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.name());
            ps.setString(2, product.brand());
            ps.setString(3, product.categoryType().name());
            ps.setString(4, product.targetAudience() == null ? null : product.targetAudience().name());
            ps.setString(5, product.description());
            ps.setInt(6, product.volumeML());
            ps.setBigDecimal(7, product.price());
            ps.setInt(8, product.releaseYear());
            ps.setInt(9, product.currentStock());
            ps.setBoolean(10, product.isAvailable());
            return ps;
        }, keyHolder);
        long newProductId = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("id")).longValue();

        if (product.notes() != null && !product.notes().isEmpty()) {
            String sqlForNotes = "INSERT INTO product_note (product_id,note_id) VALUES (?,?)";
            List<Object[]> valuesOfNotes = new ArrayList<>();
            for (FragranceNote note : product.notes()) {
                valuesOfNotes.add(new Object[]{newProductId, note.id()});
            }
            jdbcTemplate.batchUpdate(sqlForNotes, valuesOfNotes);
        }
        return product.withId(newProductId);
    }

    @Override
    public Boolean deleteById(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than zero");
        }
        String deleteNoteSql = "DELETE FROM product_note WHERE product_id = ?";
        jdbcTemplate.update(deleteNoteSql, id);

        String deleteProductSql = "DELETE FROM product WHERE id = ?";
        int rowAffected = jdbcTemplate.update(deleteProductSql, id);

        return rowAffected > 0;
    }

    @Override
    public List<Product> findByNote(FragranceNote note) {
        if(note == null) {
            throw new NullPointerException("note is null");
        }
        String sql = """
                SELECT\s
                 p.id AS product_id,
                 p.name AS product_name,
                 p.brand,
                 p.category_type,
                 p.description,
                 p.target_audience,
                 p.volume_ml,
                 p.price,
                 p.release_year,
                 p.current_stock,
                 p.is_available,
                 fn.id AS note_id,
                 fn.name AS note_name
                 FROM product p
                 LEFT JOIN product_note pn ON p.id = pn.product_id
                 LEFT JOIN fragrance_note fn ON pn.note_id = fn.id
                 WHERE p.id IN (
                    SELECT product_id FROM product_note WHERE note_id = ?
                 )
                 ORDER BY p.id
               \s""";

        return jdbcTemplate.query(sql, this.extractor, note.id());
    }
}
