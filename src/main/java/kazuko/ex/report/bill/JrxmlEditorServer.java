package kazuko.ex.report.bill;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import kazuko.ex.dao.JrxmlTemplateDAO;
import kazuko.ex.model.bill.BillTemplate;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import java.awt.Desktop;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * Host localhost tối giản cho Angular JRXML editor. Server chỉ bind loopback,
 * validate bằng JasperReports trước khi ghi DB và tự dừng sau hai giờ.
 */
public final class JrxmlEditorServer {
    private static final int MAX_JRXML_BYTES = 5 * 1024 * 1024;
    private static final Gson GSON = new Gson();
    private static JrxmlEditorServer activeServer;

    private final BillTemplate template;
    private final JrxmlTemplateDAO dao = new JrxmlTemplateDAO();
    private final HttpServer server;

    private JrxmlEditorServer(BillTemplate template) throws Exception {
        this.template = template;
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "jrxml-editor-http");
            thread.setDaemon(true);
            return thread;
        }));
    }

    public static synchronized URI open(BillTemplate template) throws Exception {
        return start(template, true);
    }

    /** Khởi động không mở browser để phục vụ smoke test tự động. */
    public static synchronized URI start(BillTemplate template, boolean openBrowser) throws Exception {
        if (template == null || template.getLegacyTemplateId() == null || template.getLegacyTemplateId().isBlank()) {
            throw new IllegalArgumentException("Mẫu phải được liên kết với một bản ghi STEMPLATE");
        }
        if (openBrowser && (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
            throw new IllegalStateException("Máy này không hỗ trợ mở trình duyệt mặc định");
        }
        if (activeServer != null) activeServer.server.stop(0);
        JrxmlEditorServer instance = new JrxmlEditorServer(template);
        instance.server.start();
        activeServer = instance;
        URI uri = URI.create("http://127.0.0.1:" + instance.server.getAddress().getPort() + "/");
        if (openBrowser) Desktop.getDesktop().browse(uri);
        instance.scheduleStop();
        return uri;
    }

    public static String createInitialJrxml(BillTemplate template) throws Exception {
        JasperDesign design = new BillReportRenderer().createDesign(template);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JRXmlWriter.writeReport(design, output, StandardCharsets.UTF_8.name());
        return output.toString(StandardCharsets.UTF_8);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if ("/api/template".equals(path)) {
                handleTemplateApi(exchange);
                return;
            }
            serveStatic(exchange, path);
        } catch (Exception ex) {
            sendText(exchange, 500, rootMessage(ex));
        } finally {
            exchange.close();
        }
    }

    private void handleTemplateApi(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String jrxml = dao.findByLegacyTemplateId(template.getLegacyTemplateId());
            if (jrxml == null || jrxml.isBlank()) jrxml = createInitialJrxml(template);
            String body = GSON.toJson(Map.of("name", template.getName(), "jrxml", jrxml));
            send(exchange, 200, "application/json; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
            return;
        }
        if ("PUT".equalsIgnoreCase(method)) {
            byte[] data = exchange.getRequestBody().readNBytes(MAX_JRXML_BYTES + 1);
            if (data.length > MAX_JRXML_BYTES) {
                sendText(exchange, 413, "JRXML vượt quá giới hạn 5 MB");
                return;
            }
            String editorJrxml = new String(data, StandardCharsets.UTF_8);
            String compatibleJrxml = Jrxml7To6Converter.convert(editorJrxml);
            validateJrxml(compatibleJrxml);
            dao.save(template.getLegacyTemplateId(), template.getName(), compatibleJrxml);
            sendText(exchange, 200, "Đã chuyển về JasperReports 6, compile và lưu thành công");
            return;
        }
        exchange.getResponseHeaders().set("Allow", "GET, PUT");
        sendText(exchange, 405, "Phương thức không được hỗ trợ");
    }

    private static void validateJrxml(String jrxml) throws Exception {
        if (jrxml == null || jrxml.isBlank()) throw new IllegalArgumentException("JRXML không được trống");
        try (InputStream input = new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8))) {
            JasperDesign design = JRXmlLoader.load(input);
            JasperCompileManager.compileReport(design);
        }
    }

    private static void serveStatic(HttpExchange exchange, String requestPath) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()) &&
                !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Phương thức không được hỗ trợ");
            return;
        }
        String path = "/".equals(requestPath) ? "index.html" : requestPath.substring(1);
        if (path.contains("..")) {
            sendText(exchange, 400, "Đường dẫn không hợp lệ");
            return;
        }
        String resource = "web/jrxml-editor/" + path;
        try (InputStream input = JrxmlEditorServer.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                sendText(exchange, 404, "Không tìm thấy tài nguyên editor. Hãy chạy npm run build.");
                return;
            }
            byte[] data = input.readAllBytes();
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            send(exchange, 200, contentType(path), "HEAD".equalsIgnoreCase(exchange.getRequestMethod())
                    ? new byte[0] : data);
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".js")) return "text/javascript; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        send(exchange, status, "text/plain; charset=UTF-8",
                (text == null ? "Lỗi không xác định" : text).getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:");
        exchange.sendResponseHeaders(status, data.length);
        if (data.length > 0) exchange.getResponseBody().write(data);
    }

    private void scheduleStop() {
        Timer timer = new Timer("jrxml-editor-expiry", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (JrxmlEditorServer.class) {
                    if (activeServer == JrxmlEditorServer.this) activeServer = null;
                }
                server.stop(0);
            }
        }, 2 * 60 * 60 * 1000L);
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }
}
