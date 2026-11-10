package sk.upjs.perfumestore.user;

import java.util.List;
import java.util.Optional;

public interface UserAccountDao {
    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAll();

    List<UserAccount> findAllByRole(UserAccount.Role role);

    List<UserAccount> findAllActive();

    boolean existsByEmail(String email);

    UserAccount create(UserAccount userAccount);

    void update(UserAccount userAccount);

    void deactive(UserAccount userAccount);

    void active(UserAccount userAccount);
}
