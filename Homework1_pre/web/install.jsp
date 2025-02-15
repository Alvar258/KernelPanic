<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import = "java.sql.*" %>
<%@ page import="java.text.SimpleDateFormat" %>


<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Database SQL Load</title>
</head>
<style>
    .error {
        color: red;
    }
    pre {
        color: green;
    }
</style>
<body>
    <h2>Database SQL Load</h2>
    <%
        String dbname = "sob_grup_51";
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/" + dbname, "root", "root");
        Statement stmt = con.createStatement();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = "'" + formatter.format(new java.util.Date()) + "'"; // Comillas para SQL


        String[] data = {
            
            // Estados
            "INSERT INTO STATE (id, name) VALUES (NEXT VALUE FOR Estado_Gen, 'Abierto')",
            "INSERT INTO STATE (id, name) VALUES (NEXT VALUE FOR Estado_Gen, 'En proceso')",
            "INSERT INTO STATE (id, name) VALUES (NEXT VALUE FOR Estado_Gen, 'Cerrado')",

            // Municipios
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Altafulla')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Constantí')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Creixell')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Perafort')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Renau')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Roda de Berà')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Salomó')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Salou')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Tarragona')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Torredembarra')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Vespella de Gaià')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Vila-Seca')",
            "INSERT INTO Municipio (id, name) VALUES (NEXT VALUE FOR Municipio_Gen, 'Vilallonga del Camp')",


            
            // Tipos de Incidencia
            "INSERT INTO TiposIncidencia (id, name) VALUES (NEXT VALUE FOR TiposIncidencia_Gen, 'Infraestructura')",
            "INSERT INTO TiposIncidencia (id, name) VALUES (NEXT VALUE FOR TiposIncidencia_Gen, 'Vial')",
            "INSERT INTO TiposIncidencia (id, name) VALUES (NEXT VALUE FOR TiposIncidencia_Gen, 'Limpieza')",

            //Credenciales
            "INSERT INTO CREDENTIALS (id, password, username) VALUES (NEXT VALUE FOR Credentials_Gen, 'user1pass', 'user1')",
            "INSERT INTO CREDENTIALS (id, password, username) VALUES (NEXT VALUE FOR Credentials_Gen, 'user2pass', 'user2')",
            "INSERT INTO CREDENTIALS (id, password, username) VALUES (NEXT VALUE FOR Credentials_Gen, 'user3pass', 'user3')",
 
            // Usuarios
            "INSERT INTO Usuario (id, cityHall, ImageURL) VALUES (NEXT VALUE FOR Usuario_Gen, 1, 'http://example.com/image1.jpg')",
            "INSERT INTO Usuario (id, cityHall, ImageURL) VALUES (NEXT VALUE FOR Usuario_Gen, 0, 'http://example.com/image2.jpg')",
        
            // Incidencias
            "INSERT INTO Incidencia (id, dateInitial, dateFinished, description, emoji, likes, score_IA, score_final, x, y, street, municipio_id, estado_id, tiposincidencia_id) " +
            "VALUES (NEXT VALUE FOR Incidencia_Gen, " + date + ", NULL, 'Bache en la calle principal', '⚠️', 10, 8, 8, 100, 200, 'Calle Falsa 123', 1, 1, 2)",

            // Sugerencias (corregido el nombre de la columna si es necesario)
            "INSERT INTO Sugerencia (id, dateInitial, dateFinished, description, emoji, likes, processed, score_IA, score_final, street, type, x, y, municipio_id) " +
            "VALUES (NEXT VALUE FOR Sugerencia_Gen, " + date + ", NULL, 'Instalar más árboles en el parque', '🌳', 15, 0, 9, 9, 'Plaza Central', 'Mejora Ambiental', 120, 220, 1)"
        };

        for (String query : data) {
            try {
                stmt.executeUpdate(query);
                out.println("<pre> -> " + query + "</pre>");
            } catch (SQLException e) {
                out.println("<span class='error'>Error en: " + query + " -> " + e.getMessage() + "</span><br>");
            }
        }

        stmt.close();
        con.close();
    %>

    <button onclick="window.location='<%=request.getSession().getServletContext().getContextPath()%>'">Go home</button>
</body>
</html>
