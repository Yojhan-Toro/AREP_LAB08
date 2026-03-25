package Arep.Lab07;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    static Map<String, WebMethod> endPoints = new HashMap<>();
    static String staticFilesFolder = "webroot/public";

    private static final Set<String> PUBLIC_PATHS = new HashSet<>();
    static {
        PUBLIC_PATHS.add("/login.html");
        PUBLIC_PATHS.add("/index.html");
        PUBLIC_PATHS.add("/style.css");
        PUBLIC_PATHS.add("/app.js");
        PUBLIC_PATHS.add("/favicon.ico");
        PUBLIC_PATHS.add("/");
        PUBLIC_PATHS.add("/shutdown");
    }

    // URL de la Máquina 3 (Spring) para validar tokens
    private static final String AUTH_URL =
        "https://ec2-54-227-126-222.compute-1.amazonaws.com:5000/api/me";

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static volatile boolean running = true;

    static Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css",  "text/css");
        MIME_TYPES.put("js",   "application/javascript");
        MIME_TYPES.put("png",  "image/png");
        MIME_TYPES.put("jpg",  "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(35000);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown Hook] Graceful shutdown initiated...");
            running = false;
            try { serverSocket.close(); } catch (IOException e) {}
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS))
                    threadPool.shutdownNow();
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[Shutdown Hook] Server shut down gracefully.");
        }, "shutdown-hook-thread"));

        System.out.println("Server started on port 35000. Listening for connections...");

        while (running) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if (!running) break;
                System.err.println("Accept failed: " + e.getMessage());
                break;
            }
            threadPool.submit(new ClientHandler(clientSocket));
        }

        System.out.println("Main accept loop exited.");
    }

    public static void get(String path, WebMethod wm) {
        endPoints.put(path, wm);
    }

    public static void staticfiles(String folder) {
        staticFilesFolder = folder.startsWith("/") ? folder.substring(1) : folder;
    }

    static boolean validateToken(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            URL url = new URL(AUTH_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int status = conn.getResponseCode();
            conn.disconnect();
            return status == 200;
        } catch (Exception e) {
            System.err.println("[Auth] Error validating token: " + e.getMessage());
            return false;
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                PrintWriter out   = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                boolean isFirstLine = true;
                String repath     = null;
                String struripath = null;
                String authHeader = null;

                while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
                    System.out.println("[Thread " + Thread.currentThread().getName() + "] " + inputLine);

                    if (isFirstLine) {
                        String[] flTokens = inputLine.split(" ");
                        struripath = flTokens[1];
                        URI uripath = new URI(struripath);
                        repath = uripath.getPath();
                        isFirstLine = false;
                    }

                    if (inputLine.toLowerCase().startsWith("authorization:")) {
                        authHeader = inputLine.substring("authorization:".length()).trim();
                    }
                }

                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7).trim();
                }

                boolean isPublic = repath == null
                        || PUBLIC_PATHS.contains(repath)
                        || repath.endsWith(".css")
                        || repath.endsWith(".js")
                        || repath.endsWith(".png")
                        || repath.endsWith(".jpg")
                        || repath.endsWith(".ico")
                        || repath.endsWith(".html");

                if (!isPublic && !validateToken(token)) {
                    System.out.println("[Auth] Sin token para: " + repath + " — sirviendo página puente");

                    String safeUrl = struripath.replace("'", "\\'");
                    String safeDisplay = repath + (struripath.contains("?")
                            ? struripath.substring(struripath.indexOf('?')) : "");

                    String bridge =
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                        "<title>" + safeDisplay + "</title>" +
                        "<style>" +
                        "body{font-family:sans-serif;padding:2rem;background:#f9f9f9;max-width:600px;margin:auto;}" +
                        "h3{color:#1e3a5f;}" +
                        ".result{margin-top:1rem;padding:1rem 1.2rem;background:#fff;" +
                        "border-left:4px solid #2563eb;border-radius:4px;" +
                        "font-family:monospace;font-size:1.1rem;min-height:2rem;}" +
                        ".err{border-left-color:#dc2626;color:#dc2626;}" +
                        "a{color:#2563eb;font-size:0.9rem;}" +
                        "</style></head><body>" +
                        "<a href='/index.html'>&larr; Volver al inicio</a>" +
                        "<h3>GET " + safeDisplay + "</h3>" +
                        "<div id='result' class='result'>Cargando...</div>" +
                        "<script>" +
                        "var token = localStorage.getItem('session_token');" +
                        "if (!token) { window.location.href = '/login.html'; }" +
                        "else {" +
                        "  fetch('" + safeUrl + "', {" +
                        "    headers: { 'Authorization': 'Bearer ' + token }" +
                        "  }).then(function(r) {" +
                        "    if (r.status === 401) {" +
                        "      localStorage.removeItem('session_token');" +
                        "      localStorage.removeItem('session_user');" +
                        "      window.location.href = '/login.html';" +
                        "      return '';" +
                        "    }" +
                        "    return r.text();" +
                        "  }).then(function(html) {" +
                        "    if (!html) return;" +
                        "    var doc = new DOMParser().parseFromString(html, 'text/html');" +
                        "    var text = doc.body ? doc.body.textContent.trim() : html;" +
                        "    document.getElementById('result').textContent = text;" +
                        "  }).catch(function(e) {" +
                        "    var d = document.getElementById('result');" +
                        "    d.className = 'result err';" +
                        "    d.textContent = 'Error de red: ' + e.message;" +
                        "  });" +
                        "}" +
                        "</script></body></html>";

                    out.print(bridge);
                    out.flush();
                    return;
                }

                WebMethod currentWm = endPoints.get(repath);
                String outputLine;

                if (currentWm != null) {
                    HttpRequest req  = new HttpRequest(struripath);
                    HttpResponse res = new HttpResponse();

                    outputLine = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html\r\n"
                            + "\r\n"
                            + "<!DOCTYPE html>"
                            + "<html><head><meta charset=\"UTF-8\"><title>Response</title></head>"
                            + "<body>"
                            + currentWm.execute(req, res)
                            + "</body></html>";

                } else {
                    String resourcePath = staticFilesFolder + repath;
                    InputStream fileStream = HttpServer.class.getClassLoader()
                            .getResourceAsStream(resourcePath);

                    if (fileStream != null) {
                        String ext = repath.contains(".")
                                ? repath.substring(repath.lastIndexOf('.') + 1) : "";
                        String contentType = MIME_TYPES.getOrDefault(ext, "text/html");
                        byte[] fileBytes = fileStream.readAllBytes();
                        fileStream.close();

                        String headers = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: " + contentType + "\r\n"
                                + "\r\n";
                        clientSocket.getOutputStream().write(headers.getBytes());
                        clientSocket.getOutputStream().write(fileBytes);
                        return;
                    }

                    outputLine = "HTTP/1.1 404 Not Found\r\n"
                            + "Content-Type: text/html\r\n"
                            + "\r\n"
                            + "<!DOCTYPE html>"
                            + "<html><body><h1>404 - Not Found</h1></body></html>";
                }

                out.println(outputLine);

            } catch (Exception e) {
                System.err.println("[Thread " + Thread.currentThread().getName()
                        + "] Error: " + e.getMessage());
            } finally {
                try { clientSocket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
