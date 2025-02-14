package controller;

import jakarta.servlet.http.*;
import jakarta.mvc.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;


@Controller
@Path("/profile")
public class ProfileController {
    
    @GET
    public String showProfile(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            // Necessita login -> el redirigim
            return "redirect:/app/login?redirectTo=/app/profile";
        }
        // L’usuari ja està loguejat
        // Pots carregar la seva info, articles, etc.
        return "WEB-INF/views/profile.jsp";
    }
}
