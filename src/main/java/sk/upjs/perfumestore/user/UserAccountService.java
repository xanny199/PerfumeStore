package sk.upjs.perfumestore.user;

import sk.upjs.perfumestore.config.Factory;
import sk.upjs.perfumestore.exception.BadRequestException;

import java.util.Objects;
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

        String cleanUserAccountEmail = userAccountEmail.trim().toLowerCase();

        Optional<UserAccount> userAccountOptional = userAccountDao.findByEmail(cleanUserAccountEmail);

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

    public UserAccount register(String firstName, String lastName, String email, String phone, String password) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String cleanEmail = email.trim().toLowerCase();
        String cleanPhone = (phone == null || phone.isBlank()) ? null : phone.trim();

        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        if (userAccountDao.existsByEmail(cleanEmail)) {
            throw new BadRequestException("User with this email already exists");
        }
        String passwordHash = argon2.hash(10, 65536, 1, password.toCharArray());

        UserAccount newUser = new UserAccount(null, cleanEmail, passwordHash, firstName.trim(), lastName.trim(), cleanPhone, UserAccount.Role.CLIENT, true);

        return userAccountDao.create(newUser);
    }

    public UserAccount updateAccount(UserAccount currentUser, String firstName, String lastName, String phone, String currentPassword, String newPassword, String confirmPassword) {
        if (currentUser == null || currentUser.id() == null) {
            throw new IllegalArgumentException("Current user cannot be null");
        }

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()) {
            throw new BadRequestException("First name and last name cannot be empty");
        }
        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();
        String cleanPhone = (phone == null || phone.isBlank()) ? null : phone.trim();

        boolean isProfileChanged = !cleanFirstName.equals(currentUser.firstName())
                || !cleanLastName.equals(currentUser.lastName())
                || !Objects.equals(cleanPhone, currentUser.phone());

        boolean isPasswordChangeRequested = (newPassword != null && !newPassword.isBlank())
                || (confirmPassword != null && !confirmPassword.isBlank());

        if (!isProfileChanged && !isPasswordChangeRequested) {
            return currentUser;
        }

        if (isPasswordChangeRequested) {
            if (newPassword == null || !newPassword.equals(confirmPassword)) {
                throw new BadRequestException("New passwords do not match");
            }
            if (newPassword.length() < 8) {
                throw new BadRequestException("New password must be at least 8 characters long");
            }
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BadRequestException("Current password is required to save changes");
        }
        if (!argon2.verify(currentUser.passwordHash(), currentPassword)) {
            throw new BadRequestException("Current password is incorrect");
        }
        String passwordHashToSave = isPasswordChangeRequested
                ? argon2.hash(10, 65536, 1, newPassword.toCharArray())
                : currentUser.passwordHash();

        UserAccount updated = new UserAccount(
                currentUser.id(),
                currentUser.email(),
                passwordHashToSave,
                cleanFirstName,
                cleanLastName,
                cleanPhone,
                currentUser.role(),
                currentUser.isActive()
        );
        userAccountDao.update(updated);
        return updated;
    }
}
