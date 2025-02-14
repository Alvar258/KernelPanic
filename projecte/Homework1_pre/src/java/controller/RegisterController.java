package controller;

import client.CustomerRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.entities.Customer;

/**
 * RegisterController gestiona el procés de registre de nous usuaris.
 */
@WebServlet("/register.jsp")
public class RegisterController extends HttpServlet {

    private CustomerRestClient customerClient = new CustomerRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mostrar el formulari de registre
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Recollir les dades del formulari de registre
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validar que les contrasenyes coincideixen
        if (password == null || !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Les contrasenyes no coincideixen.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        try {
            // Registrar l'usuari mitjançant el client REST
            Customer cus = new Customer();
            cus.setUsername(username);
            cus.setPassword(password);
            cus.setRole("CUSTOMER");
            customerClient.registerUser(cus); // Assigna el rol "USER" per defecte

            // Redirigir a la pàgina de login amb un missatge de success
            request.setAttribute("successMessage", "Registre completat amb èxit. Pots iniciar sessió.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } catch (Exception e) {
            // Manejar errors durant el registre
            System.err.println("Error durant el registre: " + e.getMessage());
            request.setAttribute("errorMessage", "Error durant el registre: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
