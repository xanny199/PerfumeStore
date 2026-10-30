package sk.upjs.user;

import lombok.With;

@With
public record UserAccount(
        Long id,
        String email,
        String passwordHash,
        String firstName,
        String secondName,
        String phone,
        Role role
) {
    public enum Role {
        CLIENT,
        CASHIER,
        MANAGER
    }
}
