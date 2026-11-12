package sk.upjs.perfumestore.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class JdbcUserAccountDAO implements UserAccountDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<UserAccount> rowMapper = new RowMapper<UserAccount>() {

        @Override
        public UserAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserAccount(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone"),
                    UserAccount.Role.valueOf(rs.getString("role")),
                    rs.getBoolean("is_active")
            );
        }
    };

    public JdbcUserAccountDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("Id must be greater than zero");
        }

        String sql = """
                SELECT\s
                    id,
                    email,
                    password_hash,
                    first_name,
                    last_name,
                    phone,
                    role,
                    is_active
                FROM user_account
                WHERE id=?
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper, id);
        if (resultSQL.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSQL.getFirst());
        }
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        String sql = """
                SELECT\s
                    id,
                    email,
                    password_hash,
                    first_name,
                    last_name,
                    phone,
                    role,
                    is_active
                FROM user_account
                WHERE email=?
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper, email);
        if (resultSQL.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSQL.getFirst());
        }
    }

    @Override
    public List<UserAccount> findAll() {
        String sql = """
                SELECT\s
                    id,
                    email,
                    password_hash,
                    first_name,
                    last_name,
                    phone,
                    role,
                    is_active
                FROM user_account
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper);
        return Collections.unmodifiableList(resultSQL);
    }

    @Override
    public List<UserAccount> findAllByRole(UserAccount.Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        String sql = """
                SELECT\s
                    id,
                    email,
                    password_hash,
                    first_name,
                    last_name,
                    phone,
                    role,
                    is_active
                FROM user_account
                WHERE role = CAST(? AS user_role)
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper, role.name());
        return Collections.unmodifiableList(resultSQL);
    }

    @Override
    public List<UserAccount> findAllActive() {
        String sql = """
                SELECT\s
                    id,
                    email,
                    password_hash,
                    first_name,
                    last_name,
                    phone,
                    role,
                    is_active
                FROM user_account
                WHERE is_active=?
               \s""";

        var resultSQL = jdbcTemplate.query(sql, rowMapper, true);
        return Collections.unmodifiableList(resultSQL);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM user_account
                    WHERE email = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public UserAccount create(UserAccount userAccount) {
        if (userAccount == null) {
            throw new IllegalArgumentException("UserAccount cannot be null");
        }

        if (userAccount.id() != null) {
            throw new IllegalArgumentException("Created UserAccount cannot have id");
        }

        validateUserAccountData(userAccount);

        String sql = """
                INSERT INTO user_account\s
                    (email, password_hash, first_name, last_name, phone, role, is_active)
                VALUES (?, ?, ?, ?, ?, CAST(? AS user_role), ?)
                \s""";

        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, userAccount.email());
            ps.setString(2, userAccount.passwordHash());
            ps.setString(3, userAccount.firstName());
            ps.setString(4, userAccount.lastName());
            ps.setString(5, userAccount.phone());
            ps.setString(6, userAccount.role().name());
            ps.setBoolean(7, userAccount.isActive());
            return ps;
        }, keyHolder);

        Long id = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("id")).longValue();
        return this.findById(id).orElseThrow();
    }

    @Override
    public void update(UserAccount userAccount) {
        validateUserAccountID(userAccount);
        validateUserAccountData(userAccount);

        String sql = """
                UPDATE user_account
                SET email=?, password_hash=?, first_name=?, last_name=?, phone=?, role=CAST(? AS user_role), is_active=?
                WHERE id=?
                """;

        var affectedRows = jdbcTemplate.update(
                sql,
                userAccount.email(),
                userAccount.passwordHash(),
                userAccount.firstName(),
                userAccount.lastName(),
                userAccount.phone(),
                userAccount.role().name(),
                userAccount.isActive(),
                userAccount.id()
        );

        if (affectedRows == 0) throw new RuntimeException("UserAccount was not found");
    }

    @Override
    public void deactive(UserAccount userAccount) {
        validateUserAccountID(userAccount);

        String sql = """
                UPDATE user_account
                SET is_active=?
                WHERE id=?
                """;

        var affectedRows = jdbcTemplate.update(sql, false, userAccount.id());
        if (affectedRows == 0) {
            throw new NoSuchElementException("User not found");
        }
    }

    @Override
    public void active(UserAccount userAccount) {
        validateUserAccountID(userAccount);

        String sql = """
                UPDATE user_account
                SET is_active=?
                WHERE id=?
                """;

        var affectedRows = jdbcTemplate.update(sql, true, userAccount.id());
        if (affectedRows == 0) {
            throw new NoSuchElementException("User not found");
        }
    }

    private void validateUserAccountID(UserAccount userAccount) {
        if (userAccount == null) {
            throw new IllegalArgumentException("UserAccount must not be null");
        }

        if (userAccount.id() == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        if (userAccount.id() <= 0) {
            throw new IllegalArgumentException("Id must be greater than zero");
        }
    }

    private void validateUserAccountData(UserAccount userAccount) {
        if (userAccount == null) {
            throw new IllegalArgumentException("UserAccount must not be null");
        }

        if (userAccount.email() == null || userAccount.email().isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        if (userAccount.passwordHash() == null || userAccount.passwordHash().isBlank()) {
            throw new IllegalArgumentException("Password hash must not be null or blank");
        }

        if (userAccount.firstName() == null || userAccount.firstName().isBlank()) {
            throw new IllegalArgumentException("First name must not be null or blank");
        }

        if (userAccount.lastName() == null || userAccount.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name must not be null or blank");
        }

        if (userAccount.role() == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
    }
}
