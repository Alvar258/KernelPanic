<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Registro - Vigilante Tarragona</title>
    <!-- Vincula tu archivo de estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css" />
    <!-- Iconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" />
  </head>
  <body>
    <div class="auth-background">
      <div class="auth-container">
        <h1><i class="fas fa-user-plus"></i> Crear Cuenta</h1>
        <% 
          String errorMessage = (String) request.getAttribute("errorMessage");
          if (errorMessage != null) { 
        %>
          <p class="error"><%= errorMessage %></p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/RegisterController" class="form">
          <div class="form-group">
            <label for="username"><i class="fas fa-user"></i> Usuario</label>
            <input type="text" id="username" name="username" placeholder="Elige un nombre de usuario" required />
          </div>
          <div class="form-group">
            <label for="email"><i class="fas fa-envelope"></i> Email</label>
            <input type="email" id="email" name="email" placeholder="Tu email" required />
          </div>
          <div class="form-group">
            <label for="password"><i class="fas fa-lock"></i> Contraseña</label>
            <input type="password" id="password" name="password" placeholder="Crea una contraseña" required />
          </div>
          <div class="form-group">
            <label for="confirmPassword"><i class="fas fa-lock"></i> Confirma Contraseña</label>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Repite la contraseña" required />
          </div>
          <button type="submit" class="btn-primary">
            <i class="fas fa-user-plus"></i> Registrarse
          </button>
        </form>
        <p>¿Ya tienes una cuenta? <a href="<%= request.getContextPath() %>/login.jsp" class="btn-link">Inicia Sesión</a></p>
      </div>
    </div>
  </body>
</html>
