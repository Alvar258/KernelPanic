package controller;

import client.ArticleRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.entities.ArticleDTO;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.mail.internet.ParseException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * RootController gestiona la pàgina principal amb llistat d'articles
 */
public class RootController extends HttpServlet {

    private ArticleRestClient articleClient = new ArticleRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] topicValues = request.getParameterValues("topic");
        String author = request.getParameter("author");

        List<String> topics = new ArrayList<>();
        if (topicValues != null) {
            for (String topic : topicValues) {
                if (topic != null && !topic.isBlank()) {
                    topics.add(topic.trim());
                }
            }
        }

        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

        // Verifica si el usuario es administrador
        String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

        try {
            
            if ("admin".equalsIgnoreCase(userRole)) {
            // Redirige a admin.jsp si el usuario es administrador
            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
            return;
            }
            
            // Obtener artículos
            String articlesJson = articleClient.getArticles(topics, author, jwtToken);
            System.out.println("=== Articles Request Debug ===");
            System.out.println("Topics filter: " + topics);
            System.out.println("Author filter: " + author);
            System.out.println("JWT Token present: " + (jwtToken != null));
            
            // Procesar artículos
            List<ArticleDTO> articles = parseArticlesJson(articlesJson);
            
            // Eliminar duplicados usando ID como clave
            Map<Long, ArticleDTO> uniqueArticles = new LinkedHashMap<>();
            for (ArticleDTO article : articles) {
                if (!uniqueArticles.containsKey(article.getId())) {
                    uniqueArticles.put(article.getId(), article);
                    System.out.println("Added unique article: " + article.getId() + " - " + article.getTitle());
                } else {
                    System.out.println("Skipped duplicate: " + article.getId() + " - " + article.getTitle());
                }
            }
            
            articles = new ArrayList<>(uniqueArticles.values());
            System.out.println("Final unique articles count: " + articles.size());
            System.out.println("==========================");

            // Obtener topics disponibles
            Set<String> availableTopics = new HashSet<>();
            for (ArticleDTO article : articles) {
                if (article.getTopics() != null) {
                    availableTopics.addAll(article.getTopics());
                }
            }

            request.setAttribute("availableTopics", availableTopics);
            request.setAttribute("articles", articles);
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error en RootController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error obteniendo artículos: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }


    /**
     * Extrae una lista única de temas de los artículos.
     */
    private List<String> fetchAvailableTopics(List<ArticleDTO> articles) {
        return articles.stream()
                      .flatMap(article -> article.getTopics().stream())
                      .distinct()
                      .sorted()
                      .collect(Collectors.toList());
    }

    /**
     * Extrae una lista única de autores de los artículos.
     */
    private List<String> fetchAvailableAuthors(List<ArticleDTO> articles) {
        return articles.stream()
                      .map(ArticleDTO::getAuthorName)
                      .distinct()
                      .sorted()
                      .collect(Collectors.toList());
    }

    /**
     * parseArticlesJson utilitza Jakarta JSON per parsejar el JSON rebut
     */
    private List<ArticleDTO> parseArticlesJson(String rawJson) {
        Map<Long, ArticleDTO> uniqueArticles = new LinkedHashMap<>();

        try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
            JsonArray jsonArray = reader.readArray();
            System.out.println("Parsing " + jsonArray.size() + " articles from JSON");
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.getJsonObject(i);
                ArticleDTO dto = new ArticleDTO();
                Long id = obj.getJsonNumber("id").longValue();
                
                // Solo procesar si no hemos visto este ID antes
                if (!uniqueArticles.containsKey(id)) {
                    dto.setId(id);
                    dto.setTitle(obj.getString("title", ""));
                    dto.setSummary(obj.getString("summary", ""));
                    dto.setContent(obj.getString("content", ""));
                    dto.setImageUrl(obj.getString("imageUrl", ""));
                    dto.setAuthorName(obj.getString("authorName", ""));
                    
                    // Procesar fecha
                    String publicationDateStr = obj.getString("publicationDate", "");
                    if (!publicationDateStr.isEmpty()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
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
                    if (obj.containsKey("topics") && !obj.isNull("topics")) {
                        JsonArray topicsArray = obj.getJsonArray("topics");
                        for (int j = 0; j < topicsArray.size(); j++) {
                            topics.add(topicsArray.getString(j));
                        }
                    }
                    dto.setTopics(topics);

                    uniqueArticles.put(id, dto);
                    System.out.println("Added article: " + id + " - " + dto.getTitle());
                } else {
                    System.out.println("Skipping duplicate article with ID: " + id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing articles JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>(uniqueArticles.values());
    }
}
