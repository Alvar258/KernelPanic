package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LogoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Anul·lar la sessió
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 2. Redirigir a la pàgina principal (o on vulguis)
        response.sendRedirect(request.getContextPath() + "/RootController");
    }
}
