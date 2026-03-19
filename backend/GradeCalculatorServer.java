import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.List;

/**
 * GradeCalculatorServer — Lightweight HTTP server (built-in JDK).
 *
 * - Serves static files (index.html, style.css, script.js) from the parent directory.
 * - Exposes POST /api/calculate for grade computation.
 *
 * Usage:
 *   cd backend/
 *   javac *.java
 *   java GradeCalculatorServer
 *
 * Then open http://localhost:8080 in your browser.
 */
public class GradeCalculatorServer {

    private static final int PORT = 8080;
    // Static files live one directory up from /backend
    private static Path staticDir;

    public static void main(String[] args) throws Exception {
        // Resolve the static directory (parent of backend/)
        staticDir = Paths.get(System.getProperty("user.dir")).getParent().toAbsolutePath();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoint
        server.createContext("/api/calculate", new CalculateHandler());

        // Static file serving (catch-all)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("========================================");
        System.out.println("  Grade Calculator Server is running");
        System.out.println("  Open: http://localhost:" + PORT);
        System.out.println("  Static dir: " + staticDir);
        System.out.println("========================================");
    }

    // ===================== /api/calculate =====================

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS headers
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            // Handle preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, JsonHelper.buildError("Method not allowed. Use POST."));
                return;
            }

            try {
                // Read request body
                String body;
                try (InputStream is = exchange.getRequestBody();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    body = sb.toString();
                }

                // Parse mode
                String mode = JsonHelper.parseMode(body);

                // Parse sections
                List<List<ExamEntry>> sections = JsonHelper.parseRequest(body);
                List<ExamEntry> lecture = sections.get(0);
                List<ExamEntry> lab = sections.get(1);

                // Calculate based on mode
                double lectureGrade = 0;
                double labGrade = 0;
                double finalGrade = 0;

                if ("both".equals(mode) || "lecture".equals(mode)) {
                    lectureGrade = GradeCalculator.computeSectionGrade(lecture);
                }
                if ("both".equals(mode) || "laboratory".equals(mode)) {
                    labGrade = GradeCalculator.computeSectionGrade(lab);
                }
                if ("both".equals(mode)) {
                    finalGrade = GradeCalculator.computeFinalGrade(lectureGrade, labGrade);
                }

                // Respond
                String responseJson = JsonHelper.buildResponse(mode, lectureGrade, labGrade, finalGrade);
                sendJson(exchange, 200, responseJson);

                System.out.println("[CALC] Mode=" + mode +
                                   " Lecture=" + Math.round(lectureGrade * 100.0) / 100.0 +
                                   " Lab=" + Math.round(labGrade * 100.0) / 100.0 +
                                   " Final=" + Math.round(finalGrade * 100.0) / 100.0);

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 400, JsonHelper.buildError(e.getMessage()));
            }
        }
    }

    // ===================== Static file handler =====================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Default to index.html
            if ("/".equals(path)) path = "/index.html";

            // Security: prevent directory traversal
            Path filePath = staticDir.resolve(path.substring(1)).normalize();
            if (!filePath.startsWith(staticDir)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                String notFound = "404 Not Found";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            // Determine content type
            String contentType = getContentType(filePath.toString());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    // ===================== Utility methods =====================

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}
