<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.sql.*" %>
<%@ page import="authn.PasswordUtil" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Database SQL Load - Incidències</title>
    <style>
        .error {
            color: red;
        }
        pre {
            color: green;
        }
    </style>
</head>
<body>
    <h2>Database SQL Load - Incidències</h2>
    <%
        // Configuración de conexión a Derby
        String dbname = "sob_grup_51";
        String schema = "ROOT";
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/" + dbname, "root", "root");
        Statement stmt = con.createStatement();

        // Hashear contraseñas
        String adminPassHash = PasswordUtil.hashPassword("adminpass");
        String user1PassHash = PasswordUtil.hashPassword("user1pass");
        String user2PassHash = PasswordUtil.hashPassword("user2pass");
        String user3PassHash = PasswordUtil.hashPassword("user3pass");
        String user4PassHash = PasswordUtil.hashPassword("user4pass");
        String sobPassHash    = PasswordUtil.hashPassword("sob");

        boolean hasError = false;

        // Arreglo de sentencias SQL para insertar datos
        String data[] = new String[] {
            // Insertar Usuarios (CUSTOMER)
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'admin', '" + adminPassHash + "', 'ADMIN')",
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user1', '" + user1PassHash + "', 'CUSTOMER')",
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user2', '" + user2PassHash + "', 'CUSTOMER')",
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user3', '" + user3PassHash + "', 'CUSTOMER')",
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user4', '" + user4PassHash + "', 'CUSTOMER')",
            "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'sob', '" + sobPassHash + "', 'CUSTOMER')",
            
            // Insertar Credenciales (CREDENTIALS)
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'admin', '" + adminPassHash + "', 1)",
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user1', '" + user1PassHash + "', 2)",
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user2', '" + user2PassHash + "', 3)",
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user3', '" + user3PassHash + "', 4)",
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user4', '" + user4PassHash + "', 5)",
            "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'sob', '" + sobPassHash + "', 6)",
            
            // Insertar Tipos de Incidència (INCIDENT_TYPE)
            "INSERT INTO " + schema + ".INCIDENT_TYPE (id, name) VALUES (NEXT VALUE FOR INCIDENT_TYPE_GEN, 'Pendiente')",
            "INSERT INTO " + schema + ".INCIDENT_TYPE (id, name) VALUES (NEXT VALUE FOR INCIDENT_TYPE_GEN, 'En Proceso')",
            
            // Insertar Incidencias (INCIDENT)
            // Se asume que la tabla INCIDENT tiene columnas:
            // id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 1', 'Descripción de la incidencia 1', '/Homework1_pre/images/incidents/inc1.jpg', CURRENT_TIMESTAMP, 41.1200, 1.2400, 2, 0, 1)",
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 2', 'Descripción de la incidencia 2', '/Homework1_pre/images/incidents/inc2.jpg', CURRENT_TIMESTAMP, 41.1150, 1.2450, 3, 0, 2)",
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 3', 'Descripción de la incidencia 3', '/Homework1_pre/images/incidents/inc3.jpg', CURRENT_TIMESTAMP, 41.1185, 1.2430, 4, 0, 1)",
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 4', 'Descripción de la incidencia 4', '/Homework1_pre/images/incidents/inc4.jpg', CURRENT_TIMESTAMP, 41.1170, 1.2420, 5, 0, 2)",
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 5', 'Descripción de la incidencia 5', '/Homework1_pre/images/incidents/inc5.jpg', CURRENT_TIMESTAMP, 41.1195, 1.2440, 2, 0, 1)",
            "INSERT INTO " + schema + ".INCIDENT (id, title, description, imageUrl, incidentDate, latitude, longitude, customer_id, likes, incident_type_id) " +
                "VALUES (NEXT VALUE FOR INCIDENT_GEN, 'Incidencia 6', 'Descripción de la incidencia 6', '/Homework1_pre/images/incidents/inc6.jpg', CURRENT_TIMESTAMP, 41.1160, 1.2460, 3, 0, 2)"
        };

        for (String datum : data) {
            try {
                stmt.executeUpdate(datum);
                out.println("<pre> -> " + datum + "</pre>");
            } catch (SQLException e) {
                hasError = true;
                if ("23505".equals(e.getSQLState())) { // Clave duplicada
                    out.println("<span class='error'>Clave duplicada, omitiendo: " + datum + "</span><br>");
                } else {
                    out.println("<span class='error'>SQLException al insertar datos: " + datum + "</span><br>");
                    out.println("<span class='error'>" + e.getMessage() + "</span><br>");
                }
            }
        }
        
        stmt.close();
        con.close();
        
        if (!hasError) {
            response.setHeader("Refresh", "2;url=" + request.getContextPath() + "/RootController");
            out.println("<div style='text-align: center; margin-top: 20px;'>");
            out.println("<h3 style='color: green;'>Instalación completada con éxito</h3>");
            out.println("<p>Redirigiendo a la página principal en 2 segundos...</p>");
            out.println("</div>");
        } else {
    %>
            <div style="text-align: center; margin-top: 20px;">
                <h3 style="color: red;">Se encontraron algunos errores durante la instalación</h3>
                <button onclick="window.location='<%=request.getContextPath()%>/RootController'" style="margin-top: 10px;">
                    Ir a la página principal
                </button>
            </div>
    <%
        }
    %>
    </body>
</html>
