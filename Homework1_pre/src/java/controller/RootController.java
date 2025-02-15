package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/RootController")
public class RootController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Aquí puedes cargar datos iniciales si se requieren y luego reenviar a index.jsp
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }
}
