package service;

import authn.Credentials;
import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.DatabaseMetaData;
import model.entities.Article;
import java.util.List;
import model.entities.Customer;
import model.entities.CustomerDTO;
import authn.JWTUtil;
import jakarta.persistence.NoResultException;
import authn.PasswordUtil;

@Stateless
@Path("/customer")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CustomerFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/customer
    @GET
    public Response getAllUsers(@Context UriInfo uriInfo) {
        // Consulta para obtener todos los usuarios sin filtros
        TypedQuery<Customer> query = em.createQuery("SELECT u FROM Customer u", Customer.class);
        List<Customer> users = query.getResultList();

        // Convertir cada entidad Customer en un CustomerDTO y agregar link al último artículo, si existe
        List<CustomerDTO> userDTOs = users.stream().map(user -> {
            CustomerDTO dto = new CustomerDTO(user.getId(), user.getUsername(), user.getRole());

            if (user.getArticles() != null && !user.getArticles().isEmpty()) {
                Article latestArticle = user.getArticles().stream()
                        .max((a1, a2) -> a1.getPublicationDate().compareTo(a2.getPublicationDate()))
                        .orElse(null);

                if (latestArticle != null) {
                    String articleLink = uriInfo.getBaseUriBuilder()
                            .path(ArticleFacadeREST.class)
                            .path(Long.toString(latestArticle.getId()))
                            .build()
                            .toString();
                    dto.addLink("latest_article", articleLink);
                }
            }

            return dto;
        }).toList();

        return Response.ok(userDTOs).build();
    }


    // GET /rest/api/v1/customer/{id}
    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") Long id) {
        Customer user = em.find(Customer.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        }

        // Crear DTO para evitar exponer contraseñas
        CustomerDTO dto = new CustomerDTO(user.getId(), user.getUsername(), user.getRole());
        if (user.getArticles() != null && !user.getArticles().isEmpty()) {
            Article latestArticle = user.getArticles().stream()
                                        .max((a1, a2) -> a1.getPublicationDate().compareTo(a2.getPublicationDate()))
                                        .orElse(null);
            if (latestArticle != null) {
                dto.addLink("article", "/rest/api/v1/article/" + latestArticle.getId());
            }
        }

        return Response.ok(dto).build();
    }

    // PUT /rest/api/v1/customer/{id} - Opcional: Actualizar un usuario
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

        // Verificar si el usuario autenticado es el mismo o si es un administrador
        if (!username.equals(user.getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar este usuario.").build();
        }

        try {
            // Actualizar los campos permitidos
            user.setUsername(updatedUser.getUsername());
            
            // Solo actualizar la contraseña si se proporciona una nueva
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                String hashedPassword = PasswordUtil.hashPassword(updatedUser.getPassword());
                user.setPassword(hashedPassword);
                
                // Actualizar también las credenciales
                TypedQuery<Credentials> query = em.createQuery(
                    "SELECT c FROM Credentials c WHERE c.customer.id = :customerId",
                    Credentials.class
                );
                query.setParameter("customerId", id);
                Credentials credentials = query.getSingleResult();
                credentials.setPassword(hashedPassword);
                em.merge(credentials);
            }

            if (isAdmin) {
                user.setRole(updatedUser.getRole()); // Solo los administradores pueden cambiar roles
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

    // POST /rest/api/v1/customer - Crear un nuevo usuario
    @POST
    public Response createUser(Customer user, @Context UriInfo uriInfo) {
        try {
            // Validar campos obligatorios
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

            // Validar el rol
            String role = user.getRole();
            if (role == null) {
                role = "CUSTOMER";
                user.setRole(role);
            } else if (!role.equals("CUSTOMER") && !role.equals("ADMIN")) {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("El rol del usuario no es válido. Debe ser 'CUSTOMER' o 'ADMIN'.")
                             .build();
            }

            // Verificar si el username ya existe
            TypedQuery<Credentials> query = em.createQuery(
                "SELECT c FROM Credentials c WHERE c.username = :username", 
                Credentials.class
            );
            query.setParameter("username", user.getUsername());
            List<Credentials> existingCredentials = query.getResultList();
            
            if (!existingCredentials.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                             .entity("El nombre de usuario ya existe")
                             .build();
            }

            // Hash de la contraseña antes de almacenarla
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            // Crear el cliente
            em.persist(user);
            em.flush();

            // Crear las credenciales asociadas
            Credentials credentials = new Credentials();
            credentials.setUsername(user.getUsername());
            credentials.setPassword(hashedPassword);
            credentials.setCustomer(user);
            em.persist(credentials);

            URI uri = uriInfo.getAbsolutePathBuilder()
                           .path(String.valueOf(user.getId()))
                           .build();
            
            return Response.created(uri)
                         .entity(user.getId())
                         .build();
                         
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error al crear el usuario: " + e.getMessage())
                         .build();
        }
    }


    // DELETE /rest/api/v1/customer/{id} - Opcional: Eliminar un usuario
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

        // Verificar permisos: el cliente puede eliminarse a sí mismo o debe ser un administrador
        if (!authenticatedUsername.equals(customer.getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar este cliente").build();
        }

        try {
            // Eliminar credenciales asociadas
            TypedQuery<Credentials> query = em.createQuery("SELECT c FROM Credentials c WHERE c.customer.id = :customerId", Credentials.class);
            List<Credentials> credentials = query.setParameter("customerId", id).getResultList();
            for (Credentials cred : credentials) {
                em.remove(cred);
            }

            // Eliminar artículos asociados
            TypedQuery<Article> articleQuery = em.createQuery("SELECT a FROM Article a WHERE a.author.id = :customerId", Article.class);
            List<Article> articles = articleQuery.setParameter("customerId", id).getResultList();
            for (Article article : articles) {
                em.remove(article);
            }

            // Finalmente, eliminar el cliente
            em.remove(customer);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar el cliente: " + e.getMessage()).build();
        }
    }

    // POST /rest/api/v1/customer/login - Endpoint to login and generate JWT token
    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        try {
            TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
            Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();

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
            TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
            Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();
            
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
