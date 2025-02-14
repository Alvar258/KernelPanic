package controller;

import authn.Credentials;
import client.CustomerRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import controller.LoginResponse;

/**
 * LoginController gestiona el procés d'autenticació de l'usuari.
 */
@WebServlet("/login.jsp")
public class LoginController extends HttpServlet {

    private CustomerRestClient customerClient = new CustomerRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mostrar el formulari de login
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Credentials crd = new Credentials();
            crd.setUsername(username);
            crd.setPassword(password);
            
            LoginResponse loginResponse = customerClient.login(crd);
            
            if (loginResponse != null && loginResponse.getToken() != null) {
                // Limpiar cualquier sesión existente
                request.getSession().invalidate();
                
                // Crear nueva sesión
                HttpSession session = request.getSession(true);
                
                // Establecer atributos de sesión
                session.setAttribute("username", username);
                session.setAttribute("role", loginResponse.getRole());
                session.setAttribute("jwtToken", loginResponse.getToken());
                
                // Debug logs
                System.out.println("=== Session Debug ===");
                System.out.println("New Session ID: " + session.getId());
                System.out.println("Username: " + username);
                System.out.println("Role from server: " + loginResponse.getRole());
                System.out.println("Role in session: " + session.getAttribute("role"));
                System.out.println("JWT Token: " + loginResponse.getToken().substring(0, 20) + "...");
                System.out.println("===================");

                response.sendRedirect(request.getContextPath() + "/RootController");
            } else {
                request.setAttribute("errorMessage", "Credenciales incorrectas");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error en login: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
