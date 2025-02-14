package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;

public class ResourceFilter implements Filter {
    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) {
        this.context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();

        if (uri.startsWith(request.getContextPath() + "/css/")) {
            res.setContentType("text/css");
            String relPath = uri.substring(request.getContextPath().length());
            String realPath = "/WEB-INF/resources" + relPath;
            InputStream is = context.getResourceAsStream(realPath);
            if (is != null) {
                copy(is, res.getOutputStream());
                return;
            }
        } else if (uri.startsWith(request.getContextPath() + "/js/")) {
            res.setContentType("application/javascript");
            String relPath = uri.substring(request.getContextPath().length());
            String realPath = "/WEB-INF/resources" + relPath;
            InputStream is = context.getResourceAsStream(realPath);
            if (is != null) {
                copy(is, res.getOutputStream());
                return;
            }
        } else if (uri.startsWith(request.getContextPath() + "/images/")) {
            String relPath = uri.substring(request.getContextPath().length());
            String realPath = relPath;  // Las imágenes están en web/images
            InputStream is = context.getResourceAsStream(realPath);
            
            // Establecer el tipo de contenido basado en la extensión
            String extension = getFileExtension(relPath).toLowerCase();
            switch (extension) {
                case ".jpg":
                case ".jpeg":
                    res.setContentType("image/jpeg");
                    break;
                case ".png":
                    res.setContentType("image/png");
                    break;
                case ".gif":
                    res.setContentType("image/gif");
                    break;
                default:
                    res.setContentType("application/octet-stream");
            }
            
            if (is != null) {
                copy(is, res.getOutputStream());
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) != -1) {
            target.write(buf, 0, length);
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot);
    }

    @Override
    public void destroy() {
    }
}
