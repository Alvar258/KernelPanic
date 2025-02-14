package client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

/**
 * Client REST per consumir /rest/api/v1/article utilitzant Jakarta JSON.
 */
public class ArticleRestClient {

    // Assegura't que aquesta URL reflecteix el teu context path real
    private static final String BASE_URL = "http://localhost:8080/Homework1_pre/rest/api/v1/article";

    /**
     * Obtenir una llista d'articles amb opcions de filtrat.
     */
    public String getArticles(List<String> topics, String author, String jwtToken) throws Exception {
    StringBuilder sb = new StringBuilder(BASE_URL);
    boolean hasParam = false;

    if (topics != null && !topics.isEmpty()) {
        // Dividir el Ãºnico string de topics por comas si existe
        if (topics.size() == 1 && topics.get(0).contains(",")) {
            String[] splitTopics = topics.get(0).split(",");
            for (String t : splitTopics) {
                String trimmedTopic = t.trim();
                if (!trimmedTopic.isEmpty()) {
                    sb.append(hasParam ? "&" : "?").append("topic=").append(encodeValue(trimmedTopic));
                    hasParam = true;
                }
            }
        } else {
            // Procesar la lista de topics normalmente
            for (String t : topics) {
                if (!t.trim().isEmpty()) {
                    sb.append(hasParam ? "&" : "?").append("topic=").append(encodeValue(t.trim()));
                    hasParam = true;
                }
            }
        }
    }
    if (author != null && !author.isEmpty()) {
        sb.append(hasParam ? "&" : "?").append("author=").append(encodeValue(author));
    }

    URL url = new URL(sb.toString());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    if (jwtToken != null) {
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
    }

    int status = conn.getResponseCode();
    if (status == 200) {
        try (InputStream is = conn.getInputStream();
             JsonReader reader = Json.createReader(is)) {
            JsonArray jsonArray = reader.readArray();
            StringWriter sw = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(sw)) {
                jsonWriter.writeArray(jsonArray);
            }
            return sw.toString();
        }
    } else {
        throw new Exception("HTTP " + status + " - " + conn.getResponseMessage());
    }
}


    /**
     * Obtenir un article per ID.
     */
    public String getArticleById(Long id, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("GET Article by ID URL: " + url.toString()); // Debug

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        int status = conn.getResponseCode();
        System.out.println("GET Article by ID Status: " + status); // Debug

        if (status == 200) {
            try (InputStream is = conn.getInputStream();
                 JsonReader reader = Json.createReader(is)) {
                JsonObject jsonObject = reader.readObject();
                StringWriter sw = new StringWriter();
                try (JsonWriter jsonWriter = Json.createWriter(sw)) {
                    jsonWriter.writeObject(jsonObject);
                }
                String jsonResponse = sw.toString();
                System.out.println("GET Article by ID Response: " + jsonResponse); // Debug
                return jsonResponse;
            }
        } else {
            try (InputStream is = conn.getErrorStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sbErr = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sbErr.append(line);
                throw new Exception("HTTP " + status + " - " + sbErr.toString());
            }
        }
    }

    /**
     * Crear un nou article.
     */
    public Long createArticle(String title, String summary, String content, 
                              String imageUrl, String topics, boolean isPrivate,
                              String jwtToken) throws Exception {
        URL url = new URL(BASE_URL);
        System.out.println("POST Create Article URL: " + url.toString()); // Debug

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        // ConstruÃ¯m el JSON utilitzant Jakarta JSON
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("title", title)
                .add("summary", summary)
                .add("content", content)
                .add("imageUrl", imageUrl)
                .add("isPrivate", isPrivate)
                .add("topics", buildTopicsJson(topics));

        JsonObject json = jsonBuilder.build();

        System.out.println("POST Create Article JSON: " + json.toString()); // Debug

        try (OutputStream os = conn.getOutputStream();
             JsonWriter writer = Json.createWriter(os)) {
            writer.writeObject(json);
        }

        int status = conn.getResponseCode();
        System.out.println("POST Create Article Status: " + status); // Debug

        if (status == 201) {
            // El ID de l'article es retorna com a text/plain
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String idStr = br.readLine();
                System.out.println("POST Create Article ID: " + idStr); // Debug
                return Long.valueOf(idStr);
            }
        } else {
            try (InputStream is = conn.getErrorStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sbErr = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sbErr.append(line);
                throw new Exception("HTTP " + status + " - " + sbErr.toString());
            }
        }
    }

    /**
     * Esborrar un article per ID.
     */
    public void deleteArticle(Long id, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("DELETE Article URL: " + url.toString()); // Debug

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        int status = conn.getResponseCode();
        System.out.println("DELETE Article Status: " + status); // Debug

        if (status == 204) {
            // OK, esborrat
            return;
        } else {
            try (InputStream is = conn.getErrorStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sbErr = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sbErr.append(line);
                throw new Exception("HTTP " + status + " - " + sbErr.toString());
            }
        }
    }

    /**
     * Actualitzar un article per ID.
     */
    public void updateArticle(Long id, String title, String summary, String content,
                              String imageUrl, String topics, boolean isPrivate,
                              String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("PUT Update Article URL: " + url.toString()); // Debug

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        // ConstruÃ¯m el JSON utilitzant Jakarta JSON
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("title", title)
                .add("summary", summary)
                .add("content", content)
                .add("imageUrl", imageUrl)
                .add("isPrivate", isPrivate)
                .add("topics", buildTopicsJson(topics));

        JsonObject json = jsonBuilder.build();

        System.out.println("PUT Update Article JSON: " + json.toString()); // Debug

        try (OutputStream os = conn.getOutputStream();
             JsonWriter writer = Json.createWriter(os)) {
            writer.writeObject(json);
        }

        int status = conn.getResponseCode();
        System.out.println("PUT Update Article Status: " + status); // Debug

        if (status == 200) {
            // OK
            return;
        } else {
            try (InputStream is = conn.getErrorStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sbErr = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sbErr.append(line);
                throw new Exception("HTTP " + status + " - " + sbErr.toString());
            }
        }
    }

    /**
     * Construir el JSON array para los temas.
     * @param topics Temes separats per comes.
     * @return JsonArray de temes.
     */
    private JsonArray buildTopicsJson(String topics) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        if (topics != null && !topics.isEmpty()) {
            String[] topicsArray = topics.split(",");
            for (String topic : topicsArray) {
                String trimmedTopic = topic.trim();
                if (!trimmedTopic.isEmpty()) {
                    JsonObject topicObject = Json.createObjectBuilder()
                        .add("name", trimmedTopic)
                        .build();
                    arrayBuilder.add(topicObject);
                }
            }
        }
        return arrayBuilder.build();
    }

    /**
     * FunciÃ³ per encodificar valors de parÃ metres URL.
     */
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            return value;
        }
    }
}