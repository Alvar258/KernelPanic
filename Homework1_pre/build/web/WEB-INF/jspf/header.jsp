<%@ page contentType="text/html; charset=UTF-8" %>
<header class="header">
    <!-- Logo -->
    <a class="logo" href="<%= request.getContextPath() %>/RootController">Incidencias</a>

    <!-- Botón de menú para móviles -->
    <div class="menu-toggle" id="mobile-menu">
        <span class="bar"></span>
        <span class="bar"></span>
        <span class="bar"></span>
    </div>

    <!-- Navegación -->
    <nav class="nav" id="mainNav">
        <ul>
            <li><a href="<%= request.getContextPath() %>/RootController">Inicio</a></li>
            <%
                String userRole = (String) session.getAttribute("role");
                String username = (String) session.getAttribute("username");
                boolean isAdmin = session != null && 
                                 ("ADMIN".equals(session.getAttribute("role")) || 
                                  "admin".equals(session.getAttribute("username")));

                if (username != null) {
            %>
                <% if (isAdmin) { %>
                    <li><a href="<%= request.getContextPath() %>/AdminController">Admin Panel</a></li>
                <% } %>
                <li><a href="<%= request.getContextPath() %>/ArticleController?action=new">Nuevo Artículo</a></li>
                <li><a href="<%= request.getContextPath() %>/LogoutController">Cerrar Sesión (<%= username %>)</a></li>
            <%
                } else {
            %>
                <li><a href="<%= request.getContextPath() %>/login.jsp">Iniciar Sesión</a></li>
                <li><a href="<%= request.getContextPath() %>/register.jsp">Registrarse</a></li>
            <%
                }
            %>
        </ul>
    </nav>
</header>
