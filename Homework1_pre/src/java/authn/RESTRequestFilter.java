package authn;

import java.io.IOException;
import java.security.Principal;
import java.util.StringTokenizer;
import jakarta.annotation.Priority;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.SecurityContext;
import model.entities.Usuario;
import authn.JWTUtil;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTRequestFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;
       
    public RESTRequestFilter(){}
    
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        String authHeader = requestCtx.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            System.out.println("No Authorization header present.");
            return;
        }

        String token = authHeader.replace(AUTHORIZATION_HEADER_PREFIX, "");

        try {
            String usernametoken = JWTUtil.validateToken(token);
            
            System.out.println(usernametoken);
            
            
            requestCtx.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> usernametoken;
                }

                @Override
                public boolean isSecure() {
                    return requestCtx.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "BEARER";
                }

                @Override
                public boolean isUserInRole(String role) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            });

        } catch (NoResultException e) {
            requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());
        } catch (Exception e) {
            requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build());
        }
    }

}
