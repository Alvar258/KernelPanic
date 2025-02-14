package service;

import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import model.entities.Article;
import model.entities.Customer;
import java.util.Date;
import java.util.List;
import model.entities.ArticleDTO;
import java.util.stream.Collectors;
import model.entities.Topic;


@Stateless
@Path("/article")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ArticleFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/article?topic=${topic}&author=${author}
    @GET
    public Response getArticles(@QueryParam("topic") List<String> topics, 
                                @QueryParam("author") String author, 
                                @Context SecurityContext securityContext) {
        boolean isAuthenticated = securityContext.getUserPrincipal() != null;
        boolean isAdmin = isAuthenticated && securityContext.isUserInRole("ADMIN");
        String currentUsername = isAuthenticated ? securityContext.getUserPrincipal().getName() : null;

        System.out.println("¿Usuario autenticado?: " + isAuthenticated);

        // Construir la consulta
        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM Article a WHERE 1=1");

        if (topics != null && !topics.isEmpty()) {
            queryBuilder.append(" AND EXISTS (SELECT t FROM a.topics t WHERE t.name IN :topics)");
        }

        if (author != null && !author.isEmpty()) {
            queryBuilder.append(" AND a.author.username = :author");
        }

        queryBuilder.append(" ORDER BY a.viewCount DESC");

        TypedQuery<Article> query = em.createQuery(queryBuilder.toString(), Article.class);

        if (topics != null && !topics.isEmpty()) {
            query.setParameter("topics", topics);
        }

        if (author != null && !author.isEmpty()) {
            query.setParameter("author", author);
        }

        // Consultar todos los artículos
        List<Article> articles = query.getResultList();

        // Filtrar artículos según la autenticación y rol
        List<ArticleDTO> articleDTOs = articles.stream()
            .filter(article -> {
                if (!article.isIsPrivate()) {
                    // Artículos públicos siempre se muestran
                    return true;
                } else {
                    // Artículos privados:
                    if (!isAuthenticated) {
                        // No autenticado, no ve privados
                        return false;
                    }
                    if (isAdmin) {
                        // Administrador ve todos los privados
                        return true;
                    }
                    // Usuario autenticado NO administrador, solo ve sus propios privados
                    return currentUsername.equals(article.getAuthor().getUsername());
                }
            })
            .map(article -> {
                ArticleDTO dto = new ArticleDTO(
                    article.getId(),
                    article.getTitle(),
                    article.getContent(),
                    article.getSummary(),
                    article.getImageUrl(),
                    article.getPublicationDate(),
                    article.getViewCount(),
                    article.isIsPrivate(),
                    article.getAuthor().getUsername() // Solo el nombre del autor
                );
                dto.setTopics(article.getTopics().stream().map(topic -> topic.getName()).collect(Collectors.toList()));
                return dto;
            })
            .toList();

        System.out.println("Número de artículos devueltos: " + articleDTOs.size());
        articleDTOs.forEach(dto -> System.out.println(dto.getTitle() + " (Privado: " + dto.isIsPrivate() + ")"));

        return Response.ok(articleDTOs).build();
    }





    // GET /rest/api/v1/article/{id}
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getArticle(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        try {
            Article article = em.find(Article.class, id);
            if (article == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Artículo no encontrado.")
                               .type(MediaType.TEXT_PLAIN)
                               .build();
            }

            if (article.isIsPrivate()) {
                if (securityContext.getUserPrincipal() == null) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                                   .entity("Autenticación requerida para artículos privados.")
                                   .type(MediaType.TEXT_PLAIN)
                                   .build();
                }
                String username = securityContext.getUserPrincipal().getName();
                if (!username.equals(article.getAuthor().getUsername())) {
                    return Response.status(Response.Status.FORBIDDEN)
                                   .entity("No tienes permiso para ver este artículo privado.")
                                   .type(MediaType.TEXT_PLAIN)
                                   .build();
                }
            }

            // Incrementar el contador de visualizaciones
            article.setViewCount(article.getViewCount() + 1);
            em.merge(article);

            // Construir el DTO
            ArticleDTO dto = new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getSummary(),
                article.getImageUrl(),
                article.getPublicationDate(),
                article.getViewCount(),
                article.isIsPrivate(),
                article.getAuthor().getUsername()
            );
            dto.setTopics(article.getTopics().stream().map(Topic::getName).collect(Collectors.toList()));

            return Response.ok(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error interno del servidor: " + e.getMessage())
                           .type(MediaType.TEXT_PLAIN)
                           .build();
        }
    }

    // DELETE /rest/api/v1/article/{id}
    @DELETE
    @Path("{id}")
    @Secured
    public Response deleteArticle(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }

        Article article = em.find(Article.class, id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Artículo no encontrado").build();
        }

        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        // Verificar si el usuario autenticado es el autor o un administrador
        if (!username.equals(article.getAuthor().getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar este artículo").build();
        }

        try {
            em.remove(article);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar el artículo.").build();
        }
    }



    // POST /rest/api/v1/article
    @POST
    @Secured
    public Response createArticle(Article article, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        // Verificar si el usuario está autenticado
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }

        String username = principal.getName();

        // Log para verificar el autor autenticado
        System.out.println("Usuario autenticado: " + username);

        // Verificar si el usuario existe en la base de datos
        Customer user;
        try {
            user = em.createQuery("SELECT u FROM Customer u WHERE u.username = :username", Customer.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Usuario no autentificado").build();
        }

        // Log para verificar el artículo recibido
        System.out.println("Título del artículo: " + article.getTitle());
        System.out.println("Contenido del artículo: " + article.getContent());
        System.out.println("Resumen del artículo: " + article.getSummary());
        System.out.println("¿Es privado?: " + article.isIsPrivate());
        System.out.println("Tópicos recibidos:");

        if (article.getTopics() != null && !article.getTopics().isEmpty()) {
            // Imprimir los tópicos enviados
            article.getTopics().forEach(topic -> System.out.println(" - " + topic.getName()));

            List<String> topicNames = article.getTopics().stream()
                                             .map(Topic::getName)
                                             .collect(Collectors.toList());

            // Asegurarse que no haya más de dos tópicos
            if (topicNames.size() > 2) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Máximo de 2 Topics permitidos.")
                               .build();
            }

            // Consultar los tópicos en la base de datos
            List<Topic> validTopics = em.createQuery("SELECT t FROM Topic t WHERE t.name IN :names", Topic.class)
                                        .setParameter("names", topicNames)
                                        .getResultList();

            System.out.println("Tópicos válidos encontrados:");
            validTopics.forEach(validTopic -> System.out.println(" - " + validTopic.getName()));

            if (validTopics.size() != topicNames.size()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Algunos Topics proporcionados no son válidos.")
                               .build();
            }

            // Asignar los tópicos válidos al artículo
            article.setTopics(validTopics);
        } else {
            System.out.println("No se proporcionaron tópicos.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Hace falta proporcionar al menos un Topic")
                           .build();
        }

        // Establecer el autor, fecha de publicación y otros campos automáticos
        article.setAuthor(user);
        article.setPublicationDate(new Date());
        article.setViewCount(0);

        // Validar otros campos obligatorios (por ejemplo, título y contenido)
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("El título del artículo es obligatorio.")
                           .build();
        }

        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("El contenido del artículo es obligatorio.")
                           .build();
        }

        try {
            // Mantener la coherencia de la relación en memoria
            // Si la lista es null, inicializarla antes de añadir el artículo
            if (user.getArticles() == null) {
                user.setArticles(new ArrayList<>());
            }
            user.getArticles().add(article);

            // Persistir el artículo (esto también actualizará las relaciones en la BD)
            em.persist(article);
            em.merge(user); // Opcional: asegura que el contexto de persistencia refleje el cambio en el Customer
            em.flush(); // Asegura que el ID se genere antes de construir la URI

            URI uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(article.getId())).build();

            return Response.created(uri).entity(article.getId().toString()).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al crear el artículo: " + e.getMessage())
                           .build();
        }

    }


    // PUT /rest/api/v1/article/{id} - Opcional: Actualizar un artículo
    @PUT
    @Path("{id}")
    @Secured
    public Response updateArticle(@PathParam("id") Long id, Article updatedArticle, @Context SecurityContext securityContext) {
        Article article = em.find(Article.class, id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Artículo no encontrado.").build();
        }

        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMIN");

        // Verificar si el usuario autenticado es el autor o administrador
        if (!username.equals(article.getAuthor().getUsername()) && !isAdmin) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar este artículo.").build();
        }

        // Actualizar los campos permitidos
        article.setTitle(updatedArticle.getTitle());
        article.setContent(updatedArticle.getContent());
        article.setSummary(updatedArticle.getSummary());
        article.setImageUrl(updatedArticle.getImageUrl());
        article.setIsPrivate(updatedArticle.isIsPrivate());
        // No se actualiza publicationDate ni viewCount

        em.merge(article);

        // Devolver un DTO o un mensaje personalizado
 
        ArticleDTO dto = new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getSummary(),
                article.getImageUrl(),
                article.getPublicationDate(),
                article.getViewCount(),
                article.isIsPrivate(),
                article.getAuthor().getUsername() // Solo el nombre del autor
                );       
        dto.setTopics(article.getTopics().stream().map(topic -> topic.getName()).collect(Collectors.toList()));
        return Response.ok(dto).build();
    }

    
}
