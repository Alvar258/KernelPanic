package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class ImageUploadController extends HttpServlet {
    
    private static final String UPLOAD_DIRECTORY = "images/articles"; // Ruta actualizada
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        try {
            // Obtener el archivo
            Part filePart = request.getPart("image");
            if (filePart == null) {
                response.getWriter().write("{\"error\": \"No se ha enviado ninguna imagen\"}");
                return;
            }
            
            String fileName = getSubmittedFileName(filePart);
            
            // Validar que es una imagen
            if (!isImageFile(fileName)) {
                response.getWriter().write("{\"error\": \"Solo se permiten archivos de imagen\"}");
                return;
            }
            
            // Generar un nombre único para el archivo
            String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(fileName);
            
            // Obtener la ruta absoluta del directorio de subida
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIRECTORY;
            
            // Crear el directorio si no existe
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Guardar el archivo
            Path filePath = Paths.get(uploadPath, uniqueFileName);
            Files.copy(filePart.getInputStream(), filePath);
            
            // Construir y devolver la URL de la imagen
            String imageUrl = request.getContextPath() + "/" + UPLOAD_DIRECTORY + "/" + uniqueFileName;
            response.getWriter().write("{\"url\": \"" + imageUrl + "\"}");
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
    
    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return "";
    }
    
    private boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".jpg") || 
               extension.equals(".jpeg") || 
               extension.equals(".png") || 
               extension.equals(".gif");
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot);
    }
}
