package controller;

import client.IncidentRestClient;
import client.CustomerRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
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
import model.entities.IncidentDTO;
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

@WebServlet("/admin")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 15
)
public class AdminController extends HttpServlet {
    private IncidentRestClient incidentClient;
    private CustomerRestClient customerClient;

    @Override
    public void init() throws ServletException {
        incidentClient = new IncidentRestClient();
        customerClient = new CustomerRestClient();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

            // Obtener datos de incidències
            String rawJson = incidentClient.getIncidents(jwtToken);
            List<IncidentDTO> incidents = new ArrayList<>();

            try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
                JsonArray jsonArray = reader.readArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.getJsonObject(i);
                    IncidentDTO dto = parseIncidentFromJson(obj);
                    incidents.add(dto);
                }
            }

            // Obtener datos de usuarios
            List<Customer> users = customerClient.getAllUsers(jwtToken);
            List<Customer> processedUsers = new ArrayList<>();
            for (Customer user : users) {
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                    user.setUsername("Sin Nombre");
                }
                if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                    user.setRole("SIN ROL");
                }
                processedUsers.add(user);
                System.out.println("Procesando usuario: " + user.getUsername() + ", Rol: " + user.getRole());
            }

            request.setAttribute("incidents", incidents);
            request.setAttribute("totalIncidents", incidents.size());
            request.setAttribute("users", processedUsers);
            request.setAttribute("totalUsers", processedUsers.size());

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
        
        boolean isMultipart = request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/form-data");
        System.out.println("Is multipart request: " + isMultipart);
        
        String action = null;
        if (isMultipart) {
            try {
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

            switch (action) {
                case "createIncident":
                    System.out.println("Calling createIncident...");
                    createIncident(request, response, jwtToken);
                    break;
                case "createUser":
                    System.out.println("Calling createUser...");
                    createUser(request, response);
                    break;
                case "deleteIncident":
                    System.out.println("Calling deleteIncident...");
                    deleteIncident(request, response, jwtToken);
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
            System.err.println("=== ERROR en doPost de AdminController ===");
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            doGet(request, response);
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean isAdmin = "true".equals(request.getParameter("isAdmin"));

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

        System.out.println("Creando nuevo usuario: " + username + " (Admin: " + isAdmin + ")");
        try {
            customerClient.registerUser(newUser);
            request.setAttribute("successMessage", "Usuario creado correctamente con rol: " + newUser.getRole());
        } catch (Exception e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            request.setAttribute("errorMessage", "Error creando usuario: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            Long userId = Long.parseLong(request.getParameter("id"));
            customerClient.deleteUser(userId, jwtToken);
            request.setAttribute("successMessage", "Usuario eliminado correctamente");
            System.out.println("Usuario eliminado con ID: " + userId);
        } catch (NumberFormatException e) {
            throw new Exception("ID de usuario inválido");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error al eliminar usuario: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    private void createIncident(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String imageUrl = request.getParameter("imageUrl");
            String latStr = request.getParameter("latitude");
            String lngStr = request.getParameter("longitude");

            System.out.println("=== Create Incident Debug ===");
            System.out.println("Title: " + title);
            System.out.println("Description length: " + (description != null ? description.length() : "null"));
            System.out.println("ImageUrl: " + imageUrl);
            System.out.println("Latitude: " + latStr + ", Longitude: " + lngStr);
            System.out.println("========================");

            if (title == null || title.trim().isEmpty()) {
                throw new Exception("El título es requerido");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new Exception("La descripción es requerida");
            }
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new Exception("La URL de la imagen es requerida");
            }
            if (latStr == null || lngStr == null) {
                throw new Exception("Las coordenadas son requeridas");
            }
            double latitude, longitude;
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lngStr);
            } catch (NumberFormatException nfe) {
                throw new Exception("Coordenadas inválidas");
            }

            Long newId = incidentClient.createIncident(
                title.trim(),
                description.trim(),
                imageUrl.trim(),
                latitude,
                longitude,
                jwtToken
            );

            if (newId != null) {
                request.setAttribute("successMessage", "Incidencia creada correctamente");
                System.out.println("Incident created successfully with ID: " + newId);
            } else {
                throw new Exception("Error al crear la incidencia: el servidor no devolvió un ID válido");
            }
            response.sendRedirect(request.getContextPath() + "/RootController");
        } catch (Exception e) {
            System.err.println("Error creating incident: " + e.getMessage());
            request.setAttribute("errorMessage", "Error creando la incidencia: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/newIncident.jsp").forward(request, response);
            throw e;
        }
    }

    private void deleteIncident(HttpServletRequest request, HttpServletResponse response, String jwtToken)
            throws Exception {
        try {
            Long incidentId = Long.valueOf(request.getParameter("id"));
            incidentClient.deleteIncident(incidentId, jwtToken);
            request.setAttribute("successMessage", "Incidencia eliminada correctamente");
            System.out.println("Incidencia eliminada con ID: " + incidentId);
        } catch (NumberFormatException e) {
            throw new Exception("ID de incidencia inválido");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error al eliminar la incidencia: " + e.getMessage());
            throw e;
        }
        doGet(request, response);
    }

    // (Opcional) updateIncident: implementar si se requiere

    private IncidentDTO parseIncidentFromJson(JsonObject obj) {
        IncidentDTO dto = new IncidentDTO();
        dto.setId(obj.getJsonNumber("id").longValue());
        dto.setTitle(obj.getString("title", ""));
        dto.setDescription(obj.getString("description", ""));
        dto.setImageUrl(obj.getString("imageUrl", ""));
        if (obj.containsKey("incidentDate") && !obj.isNull("incidentDate")) {
            try {
                String dateStr = obj.getString("incidentDate");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dto.setIncidentDate(sdf.parse(dateStr));
            } catch (Exception e) {
                System.err.println("Error parseando fecha: " + e.getMessage());
                dto.setIncidentDate(new Date());
            }
        } else {
            dto.setIncidentDate(new Date());
        }
        dto.setLatitude(obj.getJsonNumber("latitude").doubleValue());
        dto.setLongitude(obj.getJsonNumber("longitude").doubleValue());
        return dto;
    }
}
