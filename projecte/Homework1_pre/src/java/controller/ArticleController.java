package controller;

import client.ArticleRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.entities.ArticleDTO;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.mail.internet.ParseException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import jakarta.json.JsonValue;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ArticleController gestiona detalls, creació, edició i esborrat d'articles
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 15    // 15 MB
)
public class ArticleController extends HttpServlet {

    private ArticleRestClient articleClient = new ArticleRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "detail":
                showDetail(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteArticle(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/RootController");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }

        switch (action) {
            case "create":
                createArticle(request, response);
                break;
            case "update":
                updateArticle(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/RootController");
        }
    }

    /**
     * Mostrar el detall d'un article.
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String articleIdStr = request.getParameter("id");
        if (articleIdStr == null || articleIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }

        try {
            Long articleId = Long.parseLong(articleIdStr);
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            String username = (session != null) ? (String) session.getAttribute("username") : null;
            String role = (session != null) ? (String) session.getAttribute("role") : null;
            boolean isAdmin = "ADMIN".equals(role) || "admin".equals(username);

            System.out.println("=== Article Access Debug ===");
            System.out.println("Article ID: " + articleId);
            System.out.println("Username: " + username);
            System.out.println("Role: " + role);
            System.out.println("Is Admin: " + isAdmin);
            System.out.println("JWT Token present: " + (jwtToken != null));
            System.out.println("==========================");

            String rawJson = articleClient.getArticleById(articleId, jwtToken);
            ArticleDTO article = parseSingleArticle(rawJson);
            
            if (article != null) {
                request.setAttribute("article", article);
                request.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(request, response);
            } else {
                throw new Exception("No se pudo cargar el artículo.");
            }
        } catch (NumberFormatException e) {
            System.err.println("ID de artículo inválido: " + articleIdStr);
            request.setAttribute("errorMessage", "ID de artículo inválido");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error getting article detail: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Mostrar el formulari per crear un article nou.
     */
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            
            // Obtener todos los artículos para extraer los topics disponibles
            String allArticlesJson = articleClient.getArticles(new ArrayList<>(), null, jwtToken);
            Set<String> availableTopics = new HashSet<>();
            
            try (JsonReader reader = Json.createReader(new StringReader(allArticlesJson))) {
                JsonArray jsonArray = reader.readArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.getJsonObject(i);
                    if (obj.containsKey("topics")) {
                        JsonArray topicsArray = obj.getJsonArray("topics");
                        for (int j = 0; j < topicsArray.size(); j++) {
                            String topicName = topicsArray.getString(j);
                            availableTopics.add(topicName);
                        }
                    }
                }
            }
            
            request.setAttribute("availableTopics", availableTopics);
            request.getRequestDispatcher("/WEB-INF/views/newArticle.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error preparing new article form: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error preparing new article form: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Crear un nou article.
     */
    private void createArticle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Obtener los parámetros del formulario multipart
            String title = request.getParameter("title");
            String summary = request.getParameter("summary");
            String content = request.getParameter("content");
            String imageUrl = request.getParameter("imageUrl");
            String[] topicsArray = request.getParameterValues("topics");
            boolean isPrivate = request.getParameter("isPrivate") != null;

            // Convertir el array de topics a string
            String topics = "";
            if (topicsArray != null && topicsArray.length > 0) {
                topics = String.join(",", topicsArray);
            }

            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

            // Debug de los valores
            System.out.println("=== Create Article Debug ===");
            System.out.println("Title: " + title);
            System.out.println("Summary: " + summary);
            System.out.println("Content length: " + (content != null ? content.length() : 0));
            System.out.println("Image URL: " + imageUrl);
            System.out.println("Topics: " + topics);
            System.out.println("Is Private: " + isPrivate);
            System.out.println("JWT Token present: " + (jwtToken != null));
            System.out.println("========================");

            if (title == null || title.trim().isEmpty() ||
                summary == null || summary.trim().isEmpty() ||
                content == null || content.trim().isEmpty() ||
                imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new Exception("Todos los campos son obligatorios");
            }

            Long newId = articleClient.createArticle(title, summary, content, imageUrl, topics, isPrivate, jwtToken);
            response.sendRedirect(request.getContextPath() + "/RootController");
        } catch (Exception e) {
            System.err.println("Error creating article: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error creando el artículo: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/newArticle.jsp").forward(request, response);
        }
    }

    /**
     * Mostrar el formulari per editar un article existent.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        Long articleId = Long.valueOf(idStr);

        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

        try {
            // Obtener el artículo a editar
            String rawJson = articleClient.getArticleById(articleId, jwtToken);
            ArticleDTO article = parseSingleArticle(rawJson);

            // Obtener todos los artículos para extraer los topics disponibles
            String allArticlesJson = articleClient.getArticles(new ArrayList<>(), null, jwtToken);
            Set<String> availableTopics = new HashSet<>();
            
            try (JsonReader reader = Json.createReader(new StringReader(allArticlesJson))) {
                JsonArray jsonArray = reader.readArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.getJsonObject(i);
                    if (obj.containsKey("topics")) {
                        JsonArray topicsArray = obj.getJsonArray("topics");
                        for (int j = 0; j < topicsArray.size(); j++) {
                            String topicName = topicsArray.getString(j);
                            availableTopics.add(topicName);
                        }
                    }
                }
            }
            
            request.setAttribute("availableTopics", availableTopics);
            request.setAttribute("article", article);
            request.getRequestDispatcher("/WEB-INF/views/editArticle.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error obteniendo el artículo para editar: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error obteniendo el artículo para editar: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Actualitzar un article existent.
     */
    private void updateArticle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        Long articleId = Long.valueOf(idStr);

        try {
            // Primero obtener el artículo actual para tenerlo disponible si hay error
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            String rawJson = articleClient.getArticleById(articleId, jwtToken);
            ArticleDTO currentArticle = parseSingleArticle(rawJson);
            
            String title = request.getParameter("title");
            String summary = request.getParameter("summary");
            String content = request.getParameter("content");
            String imageUrl = request.getParameter("imageUrl");
            
            // Obtener los topics seleccionados
            String[] selectedTopics = request.getParameterValues("topics");
            String topics = "";
            if (selectedTopics != null && selectedTopics.length > 0) {
                // Filtrar topics vacíos y hacer trim
                List<String> validTopics = Arrays.stream(selectedTopics)
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
                
                System.out.println("Topics seleccionados después de filtrar:");
                validTopics.forEach(topic -> System.out.println(" - " + topic));
                
                topics = String.join(",", validTopics);
            }
            
            boolean isPrivate = "on".equals(request.getParameter("isPrivate"));

            System.out.println("=== Update Article Debug ===");
            System.out.println("Article ID: " + articleId);
            System.out.println("Selected topics (raw): " + (selectedTopics != null ? String.join(", ", selectedTopics) : "none"));
            System.out.println("Topics string final: " + topics);
            System.out.println("Is Private: " + isPrivate);
            System.out.println("==========================");

            try {
                articleClient.updateArticle(articleId, title, summary, content, imageUrl, topics, isPrivate, jwtToken);
                System.out.println("Article actualitzat amb ID: " + articleId);
                response.sendRedirect(request.getContextPath() + "/ArticleController?action=detail&id=" + articleId);
            } catch (Exception e) {
                System.err.println("Error actualitzant article: " + e.getMessage());
                // Si hay error, mantener el artículo actual y los topics disponibles
                request.setAttribute("article", currentArticle);
                
                // Obtener todos los topics disponibles
                String allArticlesJson = articleClient.getArticles(new ArrayList<>(), null, jwtToken);
                Set<String> availableTopics = new HashSet<>();
                try (JsonReader reader = Json.createReader(new StringReader(allArticlesJson))) {
                    JsonArray jsonArray = reader.readArray();
                    for (JsonValue value : jsonArray.getValuesAs(JsonValue.class)) {
                        if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                            JsonObject obj = (JsonObject) value;
                            if (obj.containsKey("topics")) {
                                JsonArray topicsArray = obj.getJsonArray("topics");
                                for (JsonValue topicValue : topicsArray) {
                                    availableTopics.add(topicValue.toString().replaceAll("\"", ""));
                                }
                            }
                        }
                    }
                }
                request.setAttribute("availableTopics", availableTopics);
                
                request.setAttribute("errorMessage", "Error actualizando el artículo: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/editArticle.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Esborrar un article existent.
     */
    private void deleteArticle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        Long articleId = Long.valueOf(idStr);

        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

        try {
            articleClient.deleteArticle(articleId, jwtToken);
            System.out.println("Article esborrat amb ID: " + articleId); // Debug
            response.sendRedirect(request.getContextPath() + "/RootController");
        } catch (Exception e) {
            System.err.println("Error esborrant article: " + e.getMessage()); // Debug
            request.setAttribute("errorMessage", "Error esborrant l'article: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /**
     * Parsejar un article individual a partir d'un JSON.
     */
    private ArticleDTO parseSingleArticle(String rawJson) {
        try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
            JsonObject obj = reader.readObject();
            ArticleDTO dto = new ArticleDTO();
            
            System.out.println("=== Parsing Article JSON ===");
            System.out.println("Raw JSON: " + rawJson);
            
            dto.setId(obj.getJsonNumber("id").longValue());
            dto.setTitle(obj.getString("title", ""));
            dto.setSummary(obj.getString("summary", ""));
            dto.setContent(obj.getString("content", ""));
            dto.setImageUrl(obj.getString("imageUrl", ""));
            dto.setAuthorName(obj.getString("authorName", ""));
            
            // Procesar fecha
            String publicationDateStr = obj.getString("publicationDate", "");
            if (!publicationDateStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date publicationDate = sdf.parse(publicationDateStr.split("T")[0]);
                    dto.setPublicationDate(publicationDate);
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + publicationDateStr);
                }
            }
            
            dto.setViewCount(obj.getInt("viewCount", 0));
            dto.setIsPrivate(obj.getBoolean("isPrivate", false));

            // Procesar topics
            List<String> topics = new ArrayList<>();
            System.out.println("Topics in JSON: " + obj.get("topics"));
            
            if (obj.containsKey("topics") && !obj.isNull("topics")) {
                JsonArray topicsArray = obj.getJsonArray("topics");
                System.out.println("Topics array size: " + topicsArray.size());
                
                for (int i = 0; i < topicsArray.size(); i++) {
                    String topic = topicsArray.getString(i);
                    System.out.println("Adding topic: " + topic);
                    topics.add(topic);
                }
            }
            
            dto.setTopics(topics);
            System.out.println("Final topics list: " + topics);
            System.out.println("=========================");
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error parsing article JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
