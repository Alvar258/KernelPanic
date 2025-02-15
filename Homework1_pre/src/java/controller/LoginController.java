package controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;

@WebServlet("/LoginController")
public class LoginController extends HttpServlet {
    
    // Ajusta la URL del endpoint REST de login según el contexto (por ejemplo, MyApp)
    private static final String LOGIN_URL = "http://localhost:8080/MyApp/rest/api/v1/usuario/login";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Usuario y contraseña son obligatorios.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }
        
        String payload = Json.createObjectBuilder()
                .add("username", username)
                .add("password", password)
                .build()
                .toString();
        
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(LOGIN_URL);
        Response restResponse = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));
        
        String token = restResponse.readEntity(String.class);
        restResponse.close();
        client.close();
        
        if (token != null && !token.isEmpty() && !token.contains("Invalid")) {
            HttpSession session = request.getSession();
            session.setAttribute("jwtToken", token);
            response.sendRedirect(request.getContextPath() + "/RootController");
        } else {
            request.setAttribute("errorMessage", "Credenciales inválidas.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
