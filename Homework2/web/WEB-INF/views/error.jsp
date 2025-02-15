<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Error - Medium-like</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <script src="<%= request.getContextPath() %>/resources/js/app.js" defer></script>
</head>
<body>
    <!-- Header -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <!-- Contenedor Principal -->
    <div class="container error-page">
        <h1>¡Ups!</h1>
        <p class="error"><%= request.getAttribute("errorMessage") %></p>
        <p>
            <a href="<%= request.getContextPath() %>/RootController">
                <button>Volver al Inicio</button>
            </a>
        </p>
    </div>
</body>
</html>
