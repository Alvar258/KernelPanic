package controller;

import client.ArticleRestClient;
import client.CustomerRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.entities.ArticleDTO;
import model.entities.Customer;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import authn.Credentials;
import java.util.Arrays;
import jakarta.servlet.annotation.MultipartConfig;

/**
 * AdminController gestiona el panel de administración, incluyendo la creación y eliminación de artículos y usuarios.
 */
@WebServlet("/admin")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 15    // 15 MB
)
public class AdminController extends HttpServlet {
    private ArticleRestClient articleClient;
    private CustomerRestClient customerClient;

    @Override
    public void init() throws ServletException {
        articleClient = new ArticleRestClient();
        customerClient = new CustomerRestClient();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

            // Obtener datos de artículos
            String rawJson = articleClient.getArticles(new ArrayList<>(), null, jwtToken);
            List<ArticleDTO> articles = new ArrayList<>();
            Set<String> availableTopics = new HashSet<>();

            try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
                JsonArray jsonArray = reader.readArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.getJsonObject(i);
                    ArticleDTO dto = parseArticleFromJson(obj);
                    articles.add(dto);
                    // Filtrar tópicos nulos o vacíos antes de agregarlos
                    for (String topic : dto.getTopics()) {
                        if (topic != null && !topic.trim().isEmpty()) {
                            availableTopics.add(topic);
                        }
                    }
                }
            }

            // Obtener datos de usuarios
            List<Customer> users = customerClient.getAllUsers(jwtToken);

            // Filtrar o asignar valores por defecto a los usuarios con campos nulos
            List<Customer> processedUsers = new ArrayList<>();
            for (Customer user : users) {
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                    user.setUsername("Sin Nombre");
                }
                if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                    user.setRole("SIN ROL");
                }
                processedUsers.add(user);
                // Log detallado
                System.out.println("Procesando usuario: Username = " + user.getUsername() + ", Rol = " + user.getRole());
            }

            // Establecer atributos para el JSP
            request.setAttribute("articles", articles);
            request.setAttribute("totalArticles", articles.size());
            request.setAttribute("users", processedUsers);
            request.setAttribute("totalUsers", processedUsers.size());
            request.setAttribute("availableTopics", availableTopics);

            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error en doGet de AdminController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error al cargar el panel de administración: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("\n=== DEBUG doPost AdminController ===");
        
        // Check if the request is multipart
        boolean isMultipart = request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/form-data");
        System.out.println("Is multipart request: " + isMultipart);
        
        String action = null;
        if (isMultipart) {
            try {
                // Get the parts of the multipart request
                for (jakarta.servlet.http.Part part : request.getParts()) {
                    if ("action".equals(part.getName())) {
                        java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        java.io.InputStream inputStream = part.getInputStream();
                        while ((length = inputStream.read(buffer)) != -1) {
                            result.write(buffer, 0, length);
                        }
                        action = result.toString("UTF-8");
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing multipart request: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            action = request.getParameter("action");
        }
        
        System.out.println("Action parameter: [" + action + "]");
        
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        System.out.println("JWT Token present: " + (jwtToken != null));

        try {
            if (action == null) {
                System.out.println("Error: action parameter is null");
                throw new ServletException("Acción no especificada");
            }

            System.out.println("Executing switch for action: " + action);
            switch (action) {
                case "createArticle":
                    System.out.println("Calling createArticle...");
                    createArticle(request, response, jwtToken);
                    break;
                case "createUser":
                    System.out.println("Calling createUser...");
                    createUser(request, response);
                    break;
                case "deleteArticle":
                    System.out.println("Calling deleteArticle...");
                    deleteArticle(request, response, jwtToken);
                    break;
                case "deleteUser":
                    System.out.println("Calling deleteUser...");
                    deleteUser(request, response, jwtToken);
                    break;
                default:
                    System.out.println("Error: Invalid action [" + action + "]");
                    throw new ServletException("Acción no válida: " + action);
            }
        } catch (Exception e) {
            System.err.println("\n=== ERROR en doPost de AdminController ===");
            System.err.println("Action parameter: [" + action + "]");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("=== FIN ERROR ===\n");
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            doGet(request, response);
        }
    }

    /**
     * Crear un nuevo usuario con validaciones.
     */
    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean isAdmin = "true".equals(request.getParameter("isAdmin"));

        // Validaciones básicas
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("El nombre de usuario es requerido");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("La contraseña es requerida");
        }

        Customer newUser = new Customer();
        newUser.setUsername(username.trim());
        newUser.setPassword(password.trim());
        newUser.setRole(isAdmin ? "ADMIN" : "CUSTOMER");

        System.out.println("Creando nuevo usuario:");
        System.out.println("Username: " + username);
        System.out.println("Is Admin?: " + isAdmin);
        System.out.println("Role being set: " + newUser.getRole());

        try {
            customerClient.registerUser(newUser);

            // Verificar que el usuario se creó correctamente
            Credentials creds = new Credentials();
            creds.setUsername(username.trim());
            creds.setPassword(password.trim());
            LoginResponse testLogin = customerClient.login(creds);

            System.out.println("Test login after creation:");
            System.out.println("Login successful: " + (testLogin != null));
            if (testLogin != null) {
                System.out.println("Role from login: " + testLogin.getRole());
            }

            request.setAttribute("successMessage", "Usuario creado correctamente con rol: " + newUser.getRole());
        } catch (Exception e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            request.setAttribute("errorMessage", "Error creando usuario: " + e.getMessage());
            throw e;
        }

        doGet(request, response);
    }

    /**
     * Eliminar un usuario existente.
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            Long userId = Long.parseLong(request.getParameter("id"));
            customerClient.deleteUser(userId, jwtToken);
            request.setAttribute("successMessage", "Usuario eliminado correctamente");
            System.out.println("Usuario eliminado con ID: " + userId);
        } catch (NumberFormatException e) {
            System.err.println("ID de usuario inválido: " + request.getParameter("id"));
            throw new Exception("ID de usuario inválido");
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            request.setAttribute("errorMessage", "Error al eliminar usuario: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    /**
     * Crear un nuevo artículo con validaciones.
     */
    private void createArticle(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            // Obtener y validar parámetros básicos
            String title = request.getParameter("title");
            String summary = request.getParameter("summary");
            String content = request.getParameter("content");
            String imageUrl = request.getParameter("imageUrl");
            String[] topicsArray = request.getParameterValues("topics");

            // Debug logging
            System.out.println("\nDebug - Received parameters for article creation:");
            System.out.println("Title: [" + title + "]");
            System.out.println("Summary length: " + (summary != null ? summary.length() : "null"));
            System.out.println("Content length: " + (content != null ? content.length() : "null"));
            System.out.println("ImageUrl: [" + imageUrl + "]");
            System.out.println("Topics array: " + (topicsArray != null ? Arrays.toString(topicsArray) : "null"));

            // Validación de campos requeridos
            if (title == null || title.trim().isEmpty()) {
                throw new Exception("El título es requerido");
            }
            if (summary == null || summary.trim().isEmpty()) {
                throw new Exception("El resumen es requerido");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new Exception("El contenido es requerido");
            }
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new Exception("La URL de la imagen es requerida");
            }

            // Validación específica de tópicos
            if (topicsArray == null || topicsArray.length == 0) {
                throw new Exception("Debe seleccionar al menos un tópico");
            }
            if (topicsArray.length > 2) {
                throw new Exception("No puede seleccionar más de dos tópicos");
            }

            // Procesar tópicos de manera simple
            String topics = String.join(",", topicsArray);

            // Flag de privacidad
            boolean isPrivate = request.getParameter("isPrivate") != null;

            System.out.println("Debug - Processed data:");
            System.out.println("Final topics string: [" + topics + "]");
            System.out.println("Is private: " + isPrivate);

            // Crear artículo
            Long articleId = articleClient.createArticle(
                title.trim(),
                summary.trim(),
                content.trim(),
                imageUrl.trim(),
                topics,
                isPrivate,
                jwtToken
            );

            if (articleId != null) {
                request.setAttribute("successMessage", "Artículo creado correctamente");
                System.out.println("Debug - Article created successfully with ID: " + articleId);
            } else {
                throw new Exception("Error al crear el artículo: el servidor no devolvió un ID válido");
            }
        } catch (Exception e) {
            System.err.println("Error creando artículo: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error al crear el artículo: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    /**
     * Eliminar un artículo existente.
     */
    private void deleteArticle(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            Long articleId = Long.parseLong(request.getParameter("id"));
            articleClient.deleteArticle(articleId, jwtToken);
            request.setAttribute("successMessage", "Artículo eliminado correctamente");
            System.out.println("Artículo eliminado con ID: " + articleId);
        } catch (NumberFormatException e) {
            System.err.println("ID de artículo inválido: " + request.getParameter("id"));
            throw new Exception("ID de artículo inválido");
        } catch (Exception e) {
            System.err.println("Error al eliminar artículo: " + e.getMessage());
            request.setAttribute("errorMessage", "Error al eliminar artículo: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    /**
     * Parsear un artículo individual a partir de un JSON.
     */
    private ArticleDTO parseArticleFromJson(JsonObject obj) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(obj.getJsonNumber("id").longValue());
        dto.setTitle(obj.getString("title", ""));
        dto.setContent(obj.getString("content", ""));
        dto.setSummary(obj.getString("summary", ""));
        dto.setImageUrl(obj.getString("imageUrl", ""));
        dto.setViewCount(obj.getInt("viewCount", 0));
        dto.setIsPrivate(obj.getBoolean("isPrivate", false));
        dto.setAuthorName(obj.getString("authorName", ""));

        if (obj.containsKey("publicationDate") && !obj.isNull("publicationDate")) {
            try {
                String dateStr = obj.getString("publicationDate");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dto.setPublicationDate(sdf.parse(dateStr));
            } catch (Exception e) {
                System.err.println("Error parseando fecha: " + e.getMessage());
                dto.setPublicationDate(new Date());
            }
        } else {
            dto.setPublicationDate(new Date());
        }

        List<String> topics = new ArrayList<>();
        if (obj.containsKey("topics") && !obj.isNull("topics")) {
            JsonArray topicsArray = obj.getJsonArray("topics");
            for (int i = 0; i < topicsArray.size(); i++) {
                String topic = topicsArray.getString(i);
                // Filtrar tópicos nulos o vacíos antes de agregarlos
                if (topic != null && !topic.trim().isEmpty()) {
                    topics.add(topic);
                }
            }
        }
        dto.setTopics(topics);

        return dto;
    }
}
