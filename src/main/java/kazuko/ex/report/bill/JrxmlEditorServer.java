package kazuko.ex.report.bill;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import kazuko.ex.dao.JrxmlTemplateDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.bill.BillTemplate;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;

/** Host localhost cho canvas JRXML, có token phiên, preview và draft/publish/version. */
public final class JrxmlEditorServer {
    private static final int MAX_JRXML_BYTES = 5 * 1024 * 1024;
    private static final Gson GSON = new Gson();
    private static JrxmlEditorServer activeServer;

    private final BillTemplate template;
    private final DonHang previewOrder;
    private final String sessionToken = UUID.randomUUID().toString();
    private final JrxmlTemplateDAO dao = new JrxmlTemplateDAO();
    private final HttpServer server;

    private JrxmlEditorServer(BillTemplate template, DonHang previewOrder) throws Exception {
        this.template = template;
        this.previewOrder = previewOrder;
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "jrxml-editor-http");
            thread.setDaemon(true);
            return thread;
        }));
    }

    public static synchronized URI open(BillTemplate template) throws Exception {
        return open(template, null);
    }

    public static synchronized URI open(BillTemplate template, DonHang previewOrder) throws Exception {
        return start(template, previewOrder, true);
    }

    /** Khởi động không mở browser để phục vụ smoke test tự động. */
    public static synchronized URI start(BillTemplate template, boolean openBrowser) throws Exception {
        return start(template, null, openBrowser);
    }

    public static synchronized URI start(BillTemplate template, DonHang previewOrder, boolean openBrowser)
            throws Exception {
        if (template == null || template.getLegacyTemplateId() == null || template.getLegacyTemplateId().isBlank()) {
            throw new IllegalArgumentException("Mẫu phải được liên kết với một bản ghi STEMPLATE");
        }
        if (openBrowser && (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
            throw new IllegalStateException("Máy này không hỗ trợ mở trình duyệt mặc định");
        }
        if (activeServer != null) activeServer.stop();
        JrxmlEditorServer instance = new JrxmlEditorServer(template, previewOrder);
        instance.server.start();
        activeServer = instance;
        URI uri = URI.create("http://127.0.0.1:" + instance.server.getAddress().getPort() +
                "/?token=" + URLEncoder.encode(instance.sessionToken, StandardCharsets.UTF_8));
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
            if (path.startsWith("/api/")) {
                if (!validToken(exchange)) {
                    sendText(exchange, 403, "Phiên thiết kế không hợp lệ hoặc đã hết hạn");
                    return;
                }
                handleApi(exchange, path);
                return;
            }
            serveStatic(exchange, path);
        } catch (Exception ex) {
            sendText(exchange, 500, rootMessage(ex));
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("/api/template".equals(path) && "GET".equals(method)) {
            sendJson(exchange, 200, templatePayload());
            return;
        }
        if (("/api/template/draft".equals(path) || "/api/template".equals(path)) && "PUT".equals(method)) {
            String jrxml = convertAndValidate(readXml(exchange));
            JrxmlTemplateDAO.TemplateState state = dao.saveDraft(
                    template.getLegacyTemplateId(), template.getName(), jrxml);
            sendJson(exchange, 200, statePayload("Đã lưu bản nháp", state));
            return;
        }
        if ("/api/template/publish".equals(path) && "POST".equals(method)) {
            String jrxml = convertAndValidate(readXml(exchange));
            dao.saveDraft(template.getLegacyTemplateId(), template.getName(), jrxml);
            JrxmlTemplateDAO.TemplateState state = dao.publish(template.getLegacyTemplateId());
            sendJson(exchange, 200, statePayload("Đã xuất bản; các lần in tiếp theo sẽ dùng phiên bản này", state));
            return;
        }
        if ("/api/template/versions".equals(path) && "GET".equals(method)) {
            sendJson(exchange, 200, dao.listPublishedVersions(template.getLegacyTemplateId()));
            return;
        }
        if ("/api/template/restore".equals(path) && "POST".equals(method)) {
            int version = Integer.parseInt(queryParameter(exchange.getRequestURI(), "version"));
            JrxmlTemplateDAO.TemplateState state = dao.restoreAsDraft(template.getLegacyTemplateId(), version);
            sendJson(exchange, 200, statePayload("Đã khôi phục thành bản nháp", state));
            return;
        }
        if ("/api/template/preview".equals(path) && "POST".equals(method)) {
            String jrxml = convertAndValidate(readXml(exchange));
            JasperPrint print = new BillPrintService().createPrintFromJrxml(previewOrder, jrxml);
            send(exchange, 200, "application/pdf", JasperExportManager.exportReportToPdf(print));
            return;
        }
        if ("/api/close".equals(path) && "POST".equals(method)) {
            sendText(exchange, 200, "Đã đóng phiên thiết kế");
            new Thread(this::stop, "jrxml-editor-stop").start();
            return;
        }
        exchange.getResponseHeaders().set("Allow", "GET, PUT, POST");
        sendText(exchange, 405, "Phương thức không được hỗ trợ");
    }

    private Map<String, Object> templatePayload() throws Exception {
        JrxmlTemplateDAO.TemplateState state = dao.getState(template.getLegacyTemplateId());
        String jrxml = state == null || state.jrxml() == null || state.jrxml().isBlank()
                ? createInitialJrxml(template) : state.jrxml();
        jrxml = ensureDataDictionary(jrxml);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", template.getName());
        result.put("paperWidth", template.getPaperWidth());
        result.put("jrxml", jrxml);
        result.put("hasLiveData", previewOrder != null);
        result.put("draftVersion", state == null ? 0 : state.draftVersion());
        result.put("publishedVersion", state == null ? 0 : state.publishedVersion());
        result.put("hasUnpublishedChanges", state == null || state.hasUnpublishedChanges());
        return result;
    }

    /** Bổ sung contract dữ liệu mới vào cả các mẫu JRXML đã lưu từ phiên bản cũ. */
    static String ensureDataDictionary(String jrxml) throws Exception {
        JasperDesign design;
        try (InputStream input = new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8))) {
            design = JRXmlLoader.load(input);
        }
        java.util.Set<String> parameters = design.getParametersList().stream()
                .map(JRParameter::getName).collect(java.util.stream.Collectors.toSet());
        for (String name : BillReportRenderer.SUPPORTED_BINDINGS) {
            if (parameters.add(name)) {
                JRDesignParameter parameter = new JRDesignParameter();
                parameter.setName(name);
                parameter.setValueClass(String.class);
                design.addParameter(parameter);
            }
        }
        java.util.Set<String> fields = design.getFieldsList().stream()
                .map(JRField::getName).collect(java.util.stream.Collectors.toSet());
        for (String name : BillReportRenderer.SUPPORTED_ITEM_FIELDS) {
            if (fields.add(name)) {
                JRDesignField field = new JRDesignField();
                field.setName(name);
                field.setValueClass(switch (name) {
                    case "quantity", "unitPrice", "discountRate", "discountAmount", "amount" -> Double.class;
                    default -> String.class;
                });
                design.addField(field);
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JRXmlWriter.writeReport(design, output, StandardCharsets.UTF_8.name());
        return output.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> statePayload(String message, JrxmlTemplateDAO.TemplateState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("draftVersion", state.draftVersion());
        result.put("publishedVersion", state.publishedVersion());
        result.put("hasUnpublishedChanges", state.hasUnpublishedChanges());
        result.put("jrxml", state.jrxml());
        return result;
    }

    private static String convertAndValidate(String editorJrxml) throws Exception {
        String compatibleJrxml = Jrxml7To6Converter.convert(editorJrxml);
        try (InputStream input = new ByteArrayInputStream(compatibleJrxml.getBytes(StandardCharsets.UTF_8))) {
            JasperDesign design = JRXmlLoader.load(input);
            JasperCompileManager.compileReport(design);
        }
        // Fill bằng dữ liệu mẫu để phát hiện field không tồn tại trước khi lưu/xuất bản.
        new BillPrintService().createPrintFromJrxml(null, compatibleJrxml);
        return compatibleJrxml;
    }

    private static String readXml(HttpExchange exchange) throws IOException {
        byte[] data = exchange.getRequestBody().readNBytes(MAX_JRXML_BYTES + 1);
        if (data.length > MAX_JRXML_BYTES) throw new IllegalArgumentException("JRXML vượt quá giới hạn 5 MB");
        return new String(data, StandardCharsets.UTF_8);
    }

    private boolean validToken(HttpExchange exchange) {
        String supplied = queryParameter(exchange.getRequestURI(), "token");
        if (supplied == null) return false;
        return MessageDigest.isEqual(sessionToken.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    private static String queryParameter(URI uri, String name) {
        String query = uri.getRawQuery();
        if (query == null) return null;
        for (String part : query.split("&")) {
            int split = part.indexOf('=');
            String key = split < 0 ? part : part.substring(0, split);
            if (name.equals(URLDecoder.decode(key, StandardCharsets.UTF_8))) {
                return URLDecoder.decode(split < 0 ? "" : part.substring(split + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
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
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        send(exchange, status, "application/json; charset=UTF-8", GSON.toJson(value).getBytes(StandardCharsets.UTF_8));
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        send(exchange, status, "text/plain; charset=UTF-8",
                (text == null ? "Lỗi không xác định" : text).getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: blob:; frame-src 'self' blob:");
        exchange.sendResponseHeaders(status, data.length);
        if (data.length > 0) exchange.getResponseBody().write(data);
    }

    private void scheduleStop() {
        Timer timer = new Timer("jrxml-editor-expiry", true);
        timer.schedule(new TimerTask() {
            @Override public void run() { stop(); }
        }, 2 * 60 * 60 * 1000L);
    }

    private void stop() {
        synchronized (JrxmlEditorServer.class) {
            if (activeServer == this) activeServer = null;
        }
        server.stop(0);
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }
}
