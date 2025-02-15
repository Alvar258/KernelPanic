package controller;

import client.IncidentRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import model.entities.IncidentDTO;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;

@WebServlet("/IncidentController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 15
)
public class IncidentController extends HttpServlet {

    private IncidentRestClient incidentClient = new IncidentRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "edit":
                showEditForm (request, response);
                break;
            case "detail":
                showDetail(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "view":
                showViewForm(request, response);
                break;
            case "getMapData":
                getMapData(request, response);
                break;
            case "delete":
                deleteIncident(request, response);
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
                createIncident(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/RootController");
        }
    }

    // Muestra el detalle de una incidencia
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String incidentIdStr = request.getParameter("id");
        if (incidentIdStr == null || incidentIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        try {
            Long incidentId = Long.parseLong(incidentIdStr);
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            System.out.println("=== Incident Access Debug ===");
            System.out.println("Incident ID: " + incidentId);
            System.out.println("JWT Token present: " + (jwtToken != null));
            String rawJson = incidentClient.getIncidentById(incidentId, jwtToken);
            try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
                JsonObject obj = reader.readObject();
                IncidentDTO incident = parseIncidentFromJson(obj);
                if (incident != null) {
                    request.setAttribute("incident", incident);
                    request.getRequestDispatcher("/WEB-INF/views/detailIncident.jsp").forward(request, response);
                } else {
                    throw new Exception("No se pudo cargar la incidencia.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting incident detail: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    // Muestra el formulario para crear una nueva incidencia (newIncident.jsp)
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/newIncident.jsp").forward(request, response);
    }

    // Muestra la vista para visualizar incidències (viewIncidents.jsp)
    private void showViewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        try {
            String incidentsJson = incidentClient.getIncidents(jwtToken);
            // Parseamos el JSON a lista de IncidentDTO usando la clase auxiliar
            List<IncidentDTO> incidents = IncidentControllerHelper.parseIncidentsJson(incidentsJson);
            request.setAttribute("incidents", incidents);
            request.getRequestDispatcher("/WEB-INF/views/viewIncidents.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error obteniendo incidencias: " + e.getMessage());
            request.setAttribute("errorMessage", "Error obteniendo incidencias: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        //if (jwtToken == null) {
            // Si no hay token, redirigimos a la página de login o a una página de error
            //comprobar que el usuario sea del ayuntamiento
            //response.sendRedirect(request.getContextPath() + "/login?error=notLogged");
            //return;
        //}
        request.getRequestDispatcher("/WEB-INF/views/editIncident.jsp").forward(request, response);
    }   

    // Endpoint para obtener los datos de incidencias para el mapa
    private void getMapData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        try {
            String incidentsJson = incidentClient.getIncidents(jwtToken);
            response.setContentType("application/json");
            response.getWriter().write(incidentsJson);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Crea una incidencia
    private void createIncident(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

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
        }
    }

    // Elimina una incidencia
    private void deleteIncident(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        try {
            Long incidentId = Long.valueOf(idStr);
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            incidentClient.deleteIncident(incidentId, jwtToken);
            System.out.println("Incidencia eliminada con ID: " + incidentId);
        } catch (Exception e) {
            System.err.println("Error al eliminar incidencia: " + e.getMessage());
            request.setAttribute("errorMessage", "Error al eliminar la incidencia: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        doGet(request, response);
    }

    // Método auxiliar para parsear el JSON a IncidentDTO
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
