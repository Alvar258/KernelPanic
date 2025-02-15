package controller;

import authn.Credentials;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.entities.Usuario;
import service.UsuarioFacadeREST;

@WebServlet("/RegisterController")
public class RegisterController extends HttpServlet {

    // Para este ejemplo, se utiliza el EJB REST del backend para crear usuarios.
    // Si se desea usar un cliente REST, se puede adaptar.
    // Aquí se asume que existe un método createUser en UsuarioFacadeREST.
    @jakarta.ejb.EJB
    private UsuarioFacadeREST usuarioFacade;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mostrar la página de registro
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email"); // aunque el modelo actual no lo usa, se puede almacenar
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Todos los campos son obligatorios.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }
        
        try {
            Usuario usuario = new Usuario();
            usuario.setCityHall(true); // Suponemos que el usuario es del ayuntamiento
            usuario.setImageURL("default.png");
            
            Credentials creds = new Credentials();
            creds.setUsername(username);
            creds.setPassword(password);
            usuario.setCredentials(creds);
            
            // Se invoca el método de creación; se ignora URI si no es necesario.
            usuarioFacade.createUser(usuario, null);
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error al registrar el usuario: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
