package service;

import authn.Credentials;
import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.SecurityContext;  // IMPORT CORRECTO
import jakarta.persistence.NoResultException;
import java.net.URI;
import java.util.List;
import model.entities.Customer;
import model.entities.CustomerDTO;
import model.entities.Incident;
import authn.JWTUtil;
import authn.PasswordUtil;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Stateless
@Path("/customer")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CustomerFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    @GET
    public Response getAllUsers(@Context UriInfo uriInfo) {
        TypedQuery<Customer> query = em.createQuery("SELECT u FROM Customer u", Customer.class);
        List<Customer> users = query.getResultList();

        List<CustomerDTO> userDTOs = users.stream().map(user -> {
            CustomerDTO dto = new CustomerDTO(user.getId(), user.getUsername(), user.getRole());
            if (user.getIncidents() != null && !user.getIncidents().isEmpty()) {
                Incident latestIncident = user.getIncidents().stream()
                        .max((i1, i2) -> i1.getIncidentDate().compareTo(i2.getIncidentDate()))
                        .orElse(null);
                if (latestIncident != null) {
                    String incidentLink = uriInfo.getBaseUriBuilder()
                            .path("incident")
                            .path(Long.toString(latestIncident.getId()))
                            .build()
                            .toString();
                    dto.addLink("latest_incident", incidentLink);
                }
            }
            return dto;
        }).toList();

        return Response.ok(userDTOs).build();
    }

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id) {
        Customer user = em.find(Customer.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        }
        CustomerDTO dto = new CustomerDTO(user.getId(), user.getUsername(), user.getRole());
        if (user.getIncidents() != null && !user.getIncidents().isEmpty()) {
            Incident latestIncident = user.getIncidents().stream()
                                        .max((i1, i2) -> i1.getIncidentDate().compareTo(i2.getIncidentDate()))
                                        .orElse(null);
            if (latestIncident != null) {
                dto.addLink("incident", "/rest/api/v1/incident/" + latestIncident.getId());
            }
        }
        return Response.ok(dto).build();
    }

    @PUT
    @Path("{id}")
    @Secured
    public Response updateUser(@PathParam("id") Long id, Customer updatedUser, @Context SecurityContext securityContext) {
        Customer user = em.find(Customer.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        }
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        if (!username.equals(user.getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar este usuario.").build();
        }
        try {
            user.setUsername(updatedUser.getUsername());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                String hashedPassword = PasswordUtil.hashPassword(updatedUser.getPassword());
                user.setPassword(hashedPassword);
                TypedQuery<authn.Credentials> query = em.createQuery(
                    "SELECT c FROM Credentials c WHERE c.customer.id = :customerId", authn.Credentials.class);
                query.setParameter("customerId", id);
                authn.Credentials credentials = query.getSingleResult();
                credentials.setPassword(hashedPassword);
                em.merge(credentials);
            }
            if (isAdmin) {
                user.setRole(updatedUser.getRole());
            }
            em.merge(user);
            return Response.ok("Usuario actualizado correctamente.").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error al actualizar el usuario: " + e.getMessage())
                         .build();
        }
    }

    @POST
    public Response createUser(Customer user, @Context UriInfo uriInfo) {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("El nombre de usuario no puede estar vacío.")
                             .build();
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("La contraseña no puede estar vacía.")
                             .build();
            }
            String role = user.getRole();
            if (role == null) {
                role = "CUSTOMER";
                user.setRole(role);
            } else if (!role.equals("CUSTOMER") && !role.equals("ADMIN")) {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("El rol del usuario no es válido. Debe ser 'CUSTOMER' o 'ADMIN'.")
                             .build();
            }
            TypedQuery<authn.Credentials> query = em.createQuery(
                "SELECT c FROM Credentials c WHERE c.username = :username", authn.Credentials.class);
            query.setParameter("username", user.getUsername());
            List<authn.Credentials> existingCredentials = query.getResultList();
            if (!existingCredentials.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                             .entity("El nombre de usuario ya existe")
                             .build();
            }
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            em.persist(user);
            em.flush();
            authn.Credentials credentials = new authn.Credentials();
            credentials.setUsername(user.getUsername());
            credentials.setPassword(hashedPassword);
            credentials.setCustomer(user);
            em.persist(credentials);
            URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(user.getId())).build();
            return Response.created(uri).entity(user.getId()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error al crear el usuario: " + e.getMessage())
                         .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteCustomer(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }
        Customer customer = em.find(Customer.class, id);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Cliente no encontrado").build();
        }
        String authenticatedUsername = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");
        if (!authenticatedUsername.equals(customer.getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar este cliente").build();
        }
        try {
            TypedQuery<authn.Credentials> query = em.createQuery("SELECT c FROM Credentials c WHERE c.customer.id = :customerId", authn.Credentials.class);
            List<authn.Credentials> credentials = query.setParameter("customerId", id).getResultList();
            for (authn.Credentials cred : credentials) {
                em.remove(cred);
            }
            if (customer.getIncidents() != null) {
                customer.getIncidents().forEach(em::remove);
            }
            em.remove(customer);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error al eliminar el cliente: " + e.getMessage())
                         .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        try {
            TypedQuery<authn.Credentials> query = em.createNamedQuery("Credentials.findUser", authn.Credentials.class);
            authn.Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();
            if (PasswordUtil.verifyPassword(credentials.getPassword(), storedCredentials.getPassword())) {
                String token = JWTUtil.generateToken(credentials.getUsername());
                return Response.ok().entity(token).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }

    public String doLogin(Credentials credentials) throws Exception {
        try {
            TypedQuery<authn.Credentials> query = em.createNamedQuery("Credentials.findUser", authn.Credentials.class);
            authn.Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();
            if (PasswordUtil.verifyPassword(credentials.getPassword(), storedCredentials.getPassword())) {
                return JWTUtil.generateToken(credentials.getUsername());
            } else {
                throw new Exception("Contraseña incorrecta");
            }
        } catch (NoResultException e) {
            throw new Exception("Usuario no existe");
        }
    }
}
