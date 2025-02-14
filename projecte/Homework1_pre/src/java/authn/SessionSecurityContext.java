package authn;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class SessionSecurityContext implements SecurityContext {
    private final String username;
    private final String role;
    private final boolean secure;

    public SessionSecurityContext(String username, String role, boolean secure) {
        this.username = username;
        this.role = role;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        if (username == null) return null;
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String checkRole) {
        if (role == null) return false;
        return role.equalsIgnoreCase(checkRole);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "SESSION_SCHEME";
    }
}
