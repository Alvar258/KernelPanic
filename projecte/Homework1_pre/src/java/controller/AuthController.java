package controller;

import authn.Credentials;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

import jakarta.ejb.EJB;
import service.CustomerFacadeREST;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.PersistenceContext;
import authn.JWTUtil;
import model.entities.Customer;

public class AuthController extends HttpServlet {

    @EJB
    private CustomerFacadeREST customerFacade; // on tens doLogin

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Simplement mostrar login.jsp
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Credentials creds = new Credentials();
        creds.setUsername(username);
        creds.setPassword(password);

        try {
            // Cridem doLogin => pot llançar Excepció "Usuario no existe", "Contraseña incorrecta", etc.
            String token = customerFacade.doLogin(creds);

            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("jwtToken", token);
            
            // Consultar el rol del usuario directamente
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.username = :username",
                Customer.class
            );
            query.setParameter("username", username);
            Customer customer = query.getSingleResult();
            session.setAttribute("role", customer.getRole());

            // Redirigim a la pàgina principal
            response.sendRedirect(request.getContextPath() + "/RootController");
        } catch (Exception e) {
            // Agafem el missatge "Usuario no existe", "Contraseña incorrecta", etc.
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
