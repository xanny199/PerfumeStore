package sk.upjs.perfumestore.user;

import sk.upjs.perfumestore.BadRequestException;

import java.util.Optional;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class UserAccountService {

    private final UserAccountDAO userAccountDao;

    private final Argon2 argon2 =
            Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public UserAccountService(UserAccountDAO userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public UserAccount login(String userAccountEmail, String userAccountPassword) {
        if (userAccountEmail == null || userAccountPassword == null) {
            throw new IllegalArgumentException("Email or password is null");
        }

        if (userAccountEmail.isBlank() || userAccountPassword.isBlank()) {
            throw new IllegalArgumentException("Email or password is blank");
        }

        Optional<UserAccount> userAccountOptional = userAccountDao.findByEmail(userAccountEmail);

        if (userAccountOptional.isEmpty()) {
            throw new BadRequestException("Invalid email or password");
        }

        UserAccount user = userAccountOptional.orElseThrow();

        if (!user.isActive()) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!argon2.verify(user.passwordHash(), userAccountPassword)) {
            throw new BadRequestException("Invalid email or password");
        }

        return user;
    }
}
