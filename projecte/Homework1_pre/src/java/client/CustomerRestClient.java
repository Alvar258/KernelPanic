package client;

import authn.Credentials;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import java.util.Objects;
import model.entities.Customer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import controller.LoginResponse;
import java.util.Base64;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.ArrayList;
import jakarta.json.JsonArray;
import jakarta.persistence.NoResultException;

/**
 * Client REST per consumir /rest/api/v1/customer utilitzant Jakarta JSON.
 */
public class CustomerRestClient {

    private static final String BASE_URL = "http://localhost:8080/Homework1_pre/rest/api/v1/customer";
    private EntityManager em;

    /**
     * Login d'un usuari.
     * @param creds Credencials de l'usuari.
     * @return Token JWT si el login és exitós.
     */
    public LoginResponse login(Credentials creds) {
        try {
            String jsonBody = Json.createObjectBuilder()
                .add("username", creds.getUsername())
                .add("password", creds.getPassword())
                .build()
                .toString();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                    JsonObject jsonResponse = reader.readObject();
                    String token = jsonResponse.getString("token");
                    String role = jsonResponse.getString("role");
                    return new LoginResponse(token, role);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }

    /**
     * Registrar un nou usuari.
     * @param user Informació del nou usuari.
     */
    public void registerUser(Customer user) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Make sure we're sending the role in the JSON exactly as it is in the database
        JsonObject json = Json.createObjectBuilder()
                .add("username", user.getUsername())
                .add("password", user.getPassword())
                .add("role", user.getRole().toUpperCase()) // Ensure it's uppercase like in the DB
                .build();

        System.out.println("POST Register JSON: " + json.toString()); // Debug

        try (OutputStream os = conn.getOutputStream();
             JsonWriter writer = Json.createWriter(os)) {
            writer.writeObject(json);
        }

        int status = conn.getResponseCode();
        System.out.println("POST Register Status: " + status); // Debug

        if (status == 201) {
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
     * Get all users.
     * @param jwtToken JWT token for authentication.
     * @return List of users.
     * @throws Exception if an error occurs.
     */
    public List<Customer> getAllUsers(String jwtToken) throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Customer> users = new ArrayList<>();
                try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                    JsonArray jsonArray = reader.readArray();
                    
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject obj = jsonArray.getJsonObject(i);
                        Customer user = new Customer();
                        user.setId(obj.getJsonNumber("id").longValue());
                        user.setUsername(obj.getString("username"));
                        user.setRole(obj.getString("role"));
                        users.add(user);
                    }
                }
                return users;
            } else {
                throw new Exception("Error getting users: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            throw new Exception("Error getting users: " + e.getMessage());
        }
    }

    public void deleteUser(Long id, String jwtToken) throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new Exception("Error deleting user: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw new Exception("Error deleting user: " + e.getMessage());
        }
    }
}
