package sk.upjs.perfumestore.user;

import lombok.With;

@With
public record UserAccount(
        Long id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String phone,
        Role role,
        Boolean isActive
) {
    public enum Role {
        CLIENT,
        CASHIER,
        MANAGER
    }
}
