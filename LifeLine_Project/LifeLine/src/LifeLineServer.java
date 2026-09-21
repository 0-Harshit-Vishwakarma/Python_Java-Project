import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifeLineServer {

    private static final Path USERS_FILE = Paths.get("data", "users.json");
    private static final Path CARDS_FILE = Paths.get("data", "cards.json");
    private static final String PUBLIC_DIR = "public";
    private static final int DEFAULT_PORT = 8787;

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get("data"));
        ensureFile(USERS_FILE, "[]");
        ensureFile(CARDS_FILE, "[]");

        int port = getPort();

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port),
                0
        );

        server.createContext("/api/auth/register", LifeLineServer::register);
        server.createContext("/api/auth/login", LifeLineServer::login);
        server.createContext("/api/auth/change-password", LifeLineServer::changePassword);
        server.createContext("/api/auth/delete", LifeLineServer::deleteAccount);
        server.createContext("/api/cards", LifeLineServer::cardsApi);
        server.createContext("/", LifeLineServer::serveStatic);

        server.setExecutor(null);
        server.start();

        System.out.println("==========================================");
        System.out.println("           LifeLine Application");
        System.out.println("==========================================");
        System.out.println("LifeLine is running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static int getPort() {
        String value = System.getenv("PORT");

        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    private static void ensureFile(Path file, String content) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE
            );
        }
    }

    private static void register(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> body = parseJsonObject(readBody(exchange));

        String name = clean(body.get("name"));
        String email = clean(body.get("email")).toLowerCase(Locale.ROOT);
        String password = body.getOrDefault("password", "");

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Please fill all required fields.\"}");
            return;
        }

        if (password.length() < 4) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Password must be at least 4 characters.\"}");
            return;
        }

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> users = readJsonArray(USERS_FILE);

            for (Map<String, String> user : users) {
                if (email.equalsIgnoreCase(user.getOrDefault("email", ""))) {
                    sendJson(exchange, 409, "{\"success\":false,\"message\":\"Email is already registered.\"}");
                    return;
                }
            }

            Map<String, String> user = new LinkedHashMap<>();
            user.put("id", nextId(users, "USR"));
            user.put("name", name);
            user.put("email", email);
            user.put("password", password);

            users.add(user);
            writeJsonArray(USERS_FILE, users);

            sendJson(exchange, 201,
                    "{\"success\":true,\"message\":\"Registration successful\",\"user\":"
                            + objectToJson(user, false) + "}");
        }
    }

    private static void login(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> body = parseJsonObject(readBody(exchange));
        String email = clean(body.get("email")).toLowerCase(Locale.ROOT);
        String password = body.getOrDefault("password", "");

        List<Map<String, String>> users = readJsonArray(USERS_FILE);

        for (Map<String, String> user : users) {
            if (email.equalsIgnoreCase(user.getOrDefault("email", ""))
                    && password.equals(user.getOrDefault("password", ""))) {

                sendJson(exchange, 200,
                        "{\"success\":true,\"message\":\"Login successful\",\"user\":"
                                + objectToJson(user, false) + "}");
                return;
            }
        }

        sendJson(exchange, 401,
                "{\"success\":false,\"message\":\"Invalid email or password.\"}");
    }

    private static void changePassword(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> body = parseJsonObject(readBody(exchange));

        String email = clean(body.get("email")).toLowerCase(Locale.ROOT);
        String newPassword = body.getOrDefault("newPassword", "");
        String confirmPassword = body.getOrDefault("confirmPassword", "");

        if (email.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"Please enter your email, new password and confirm password.\"}");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"New Password and Confirm Password must match.\"}");
            return;
        }

        if (newPassword.length() < 4) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"New password must be at least 4 characters.\"}");
            return;
        }

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> users = readJsonArray(USERS_FILE);

            for (Map<String, String> user : users) {
                if (email.equalsIgnoreCase(user.getOrDefault("email", ""))) {
                    user.put("password", newPassword);
                    writeJsonArray(USERS_FILE, users);

                    sendJson(exchange, 200,
                            "{\"success\":true,\"message\":\"Password changed successfully\"}");
                    return;
                }
            }

            sendJson(exchange, 404,
                    "{\"success\":false,\"message\":\"No account found with this email address.\"}");
        }
    }

    private static void deleteAccount(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> body = parseJsonObject(readBody(exchange));
        String userId = clean(body.get("userId"));

        if (userId.isBlank()) {
            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"User account could not be identified.\"}");
            return;
        }

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> users = readJsonArray(USERS_FILE);
            boolean removed = users.removeIf(user ->
                    userId.equals(user.getOrDefault("id", "")));

            if (!removed) {
                sendJson(exchange, 404,
                        "{\"success\":false,\"message\":\"User account not found.\"}");
                return;
            }

            writeJsonArray(USERS_FILE, users);

            List<Map<String, String>> cards = readJsonArray(CARDS_FILE);
            cards.removeIf(card -> userId.equals(card.getOrDefault("userId", "")));
            writeJsonArray(CARDS_FILE, cards);

            sendJson(exchange, 200,
                    "{\"success\":true,\"message\":\"Account deleted successfully\"}");
        }
    }

    private static void cardsApi(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            String userId = queryParam(exchange.getRequestURI().getRawQuery(), "userId");

            if (userId == null || userId.isBlank()) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"User ID is required.\"}");
                return;
            }

            List<Map<String, String>> cards = readJsonArray(CARDS_FILE);
            List<Map<String, String>> result = new ArrayList<>();

            for (Map<String, String> card : cards) {
                if (userId.equals(card.getOrDefault("userId", ""))) {
                    result.add(card);
                }
            }

            sendJson(exchange, 200,
                    "{\"success\":true,\"cards\":" + arrayToJson(result) + "}");
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            createCard(exchange);
            return;
        }

        if ("PUT".equalsIgnoreCase(method)) {
            updateCard(exchange);
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            deleteCard(exchange);
            return;
        }

        sendJson(exchange, 405,
                "{\"success\":false,\"message\":\"Method not allowed\"}");
    }

    private static void createCard(HttpExchange exchange) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));

        if (clean(body.get("userId")).isBlank() || clean(body.get("name")).isBlank()) {
            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Name and user are required.\"}");
            return;
        }

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> cards = readJsonArray(CARDS_FILE);

            for (Map<String, String> existing : cards) {
                if (body.get("userId").equals(existing.getOrDefault("userId", ""))) {
                    sendJson(exchange, 409,
                            "{\"success\":false,\"message\":\"You already have an emergency card. Please update it instead.\"}");
                    return;
                }
            }

            Map<String, String> card = cardFromBody(body);
            card.put("id", nextId(cards, "CARD"));
            card.put("createdAt", now());
            card.put("updatedAt", now());

            cards.add(card);
            writeJsonArray(CARDS_FILE, cards);

            sendJson(exchange, 201,
                    "{\"success\":true,\"message\":\"Card added successfully\",\"card\":"
                            + objectToJson(card, false) + "}");
        }
    }

    private static void updateCard(HttpExchange exchange) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        String cardId = clean(body.get("id"));
        String userId = clean(body.get("userId"));

        if (cardId.isBlank() || userId.isBlank()) {
            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Card ID and user ID are required.\"}");
            return;
        }

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> cards = readJsonArray(CARDS_FILE);
            Map<String, String> target = null;

            for (Map<String, String> card : cards) {
                if (cardId.equals(card.getOrDefault("id", ""))
                        && userId.equals(card.getOrDefault("userId", ""))) {
                    target = card;
                    break;
                }
            }

            if (target == null) {
                sendJson(exchange, 404,
                        "{\"success\":false,\"message\":\"Emergency card not found.\"}");
                return;
            }

            Map<String, String> updated = cardFromBody(body);
            updated.put("id", cardId);
            updated.put("createdAt", target.getOrDefault("createdAt", now()));
            updated.put("updatedAt", now());

            cards.remove(target);
            cards.add(updated);
            writeJsonArray(CARDS_FILE, cards);

            sendJson(exchange, 200,
                    "{\"success\":true,\"message\":\"Card updated successfully\",\"card\":"
                            + objectToJson(updated, false) + "}");
        }
    }

    private static void deleteCard(HttpExchange exchange) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        String cardId = clean(body.get("id"));
        String userId = clean(body.get("userId"));

        synchronized (LifeLineServer.class) {
            List<Map<String, String>> cards = readJsonArray(CARDS_FILE);
            boolean removed = cards.removeIf(card ->
                    cardId.equals(card.getOrDefault("id", ""))
                            && userId.equals(card.getOrDefault("userId", "")));

            if (!removed) {
                sendJson(exchange, 404,
                        "{\"success\":false,\"message\":\"Emergency card not found.\"}");
                return;
            }

            writeJsonArray(CARDS_FILE, cards);

            sendJson(exchange, 200,
                    "{\"success\":true,\"message\":\"Card deleted successfully\"}");
        }
    }

    private static Map<String, String> cardFromBody(Map<String, String> body) {
        Map<String, String> card = new LinkedHashMap<>();

        String[] fields = {
                "userId", "name", "dobAge", "phone", "address",
                "emergencyName", "emergencyPhone", "relationship",
                "bloodGroup", "allergies", "medicalConditions",
                "currentMedications", "importantNotes",
                "doctorName", "doctorPhone"
        };

        for (String field : fields) {
            card.put(field, clean(body.get(field)));
        }

        return card;
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed", "text/plain");
            return;
        }

        String path = URLDecoder.decode(exchange.getRequestURI().getPath(), StandardCharsets.UTF_8);

        if (path.equals("/")) {
            path = "/index.html";
        }

        if (path.contains("..")) {
            sendText(exchange, 403, "Forbidden", "text/plain");
            return;
        }

        Path file = Paths.get(PUBLIC_DIR, path.substring(1)).normalize();

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            sendText(exchange, 404, "Not Found", "text/plain");
            return;
        }

        byte[] content = Files.readAllBytes(file);
        String contentType = contentType(file.getFileName().toString());

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, content.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(content);
        }
    }

    private static String contentType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        sendText(exchange, status, json, "application/json; charset=UTF-8");
    }

    private static void sendText(
            HttpExchange exchange,
            int status,
            String body,
            String contentType
    ) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String now() {
        return new Date().toString();
    }

    private static String nextId(List<Map<String, String>> items, String prefix) {
        int max = 0;

        for (Map<String, String> item : items) {
            String id = item.getOrDefault("id", "");

            if (id.startsWith(prefix)) {
                try {
                    int number = Integer.parseInt(id.substring(prefix.length()));
                    max = Math.max(max, number);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return prefix + String.format("%03d", max + 1);
    }

    private static String queryParam(String query, String key) {
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2 && key.equals(URLDecoder.decode(parts[0], StandardCharsets.UTF_8))) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private static List<Map<String, String>> readJsonArray(Path file) {
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8).trim();

            if (json.isBlank() || json.equals("[]")) {
                return new ArrayList<>();
            }

            List<String> objects = splitJsonObjects(json);
            List<Map<String, String>> result = new ArrayList<>();

            for (String object : objects) {
                result.add(parseJsonObject(object));
            }

            return result;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void writeJsonArray(Path file, List<Map<String, String>> items) throws IOException {
        Files.writeString(
                file,
                arrayToJson(items),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static String arrayToJson(List<Map<String, String>> items) {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < items.size(); i++) {
            json.append("  ").append(objectToJson(items.get(i), true));

            if (i < items.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    private static String objectToJson(Map<String, String> object, boolean pretty) {
        StringBuilder json = new StringBuilder("{");

        int index = 0;

        for (Map.Entry<String, String> entry : object.entrySet()) {
            if (index++ > 0) {
                json.append(",");
            }

            if (pretty) {
                json.append("\n    ");
            }

            json.append("\"")
                    .append(escapeJson(entry.getKey()))
                    .append("\": \"")
                    .append(escapeJson(entry.getValue()))
                    .append("\"");
        }

        if (pretty && !object.isEmpty()) {
            json.append("\n  ");
        }

        json.append("}");
        return json.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();

        if (json == null) {
            return result;
        }

        Pattern pattern = Pattern.compile(
                "\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            result.put(unescapeJson(matcher.group(1)), unescapeJson(matcher.group(2)));
        }

        return result;
    }

    private static String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();

        boolean escaping = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (escaping) {
                switch (ch) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(ch);
                }

                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                result.append(ch);
            }
        }

        if (escaping) {
            result.append('\\');
        }

        return result.toString();
    }

    private static List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        boolean escaping = false;
        int start = -1;

        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (escaping) {
                escaping = false;
                continue;
            }

            if (ch == '\\' && inString) {
                escaping = true;
                continue;
            }

            if (ch == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (ch == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (ch == '}') {
                depth--;

                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        return objects;
    }
}
