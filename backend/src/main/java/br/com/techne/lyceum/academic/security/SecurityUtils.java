package br.com.techne.lyceum.academic.security;

import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.shared.exception.ForbiddenException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("UNAUTHENTICATED", "Authentication is required");
        }
        return principal;
    }

    public static UUID currentUserPublicId() {
        return currentUser().getPublicId();
    }

    public static boolean isAdmin() {
        return currentUser().isAdmin();
    }

    public static void requireSelfOrAdmin(UUID targetPublicId) {
        UserPrincipal principal = currentUser();
        if (!principal.isAdmin() && !principal.getPublicId().equals(targetPublicId)) {
            throw new ForbiddenException(
                    "ACCESS_DENIED", "You can only access your own user account");
        }
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("ACCESS_DENIED", "Admin role is required");
        }
    }

    public static UserRole currentRole() {
        return currentUser().getRole();
    }
}
