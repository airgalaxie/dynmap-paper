package org.dynmap.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.dynmap.ConfigurationNode;
import org.dynmap.DynmapCore;
import org.dynmap.DynmapWorld;
import org.dynmap.Log;
import org.dynmap.PlayerFaces;
import org.dynmap.storage.MapStorage;
import org.dynmap.storage.MapStorageTile;
import org.dynmap.utils.BufferInputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaticFileWebServer {
    private static final int DEFAULT_PORT = 8123;
    private static final int DEFAULT_MAX_SESSIONS = 30;
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put("html", "text/html; charset=utf-8");
        MIME_TYPES.put("htm", "text/html; charset=utf-8");
        MIME_TYPES.put("css", "text/css; charset=utf-8");
        MIME_TYPES.put("js", "application/javascript; charset=utf-8");
        MIME_TYPES.put("json", "application/json; charset=utf-8");
        MIME_TYPES.put("txt", "text/plain; charset=utf-8");
        MIME_TYPES.put("xml", "application/xml; charset=utf-8");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("map", "application/json; charset=utf-8");
        MIME_TYPES.put("php", "application/x-httpd-php; charset=utf-8");
    }

    private final DynmapCore core;
    private final ConfigurationNode configuration;
    private final File dataDirectory;
    private final Path webRoot;
    private final Path normalizedWebRoot;
    private final Path realWebRoot;
    private final boolean allowSymlinks;
    private HttpServer httpServer;
    private HttpServer httpsServer;
    private ExecutorService executor;

    public StaticFileWebServer(DynmapCore core, ConfigurationNode configuration, File dataDirectory, String webpath) throws IOException {
        this.core = core;
        this.configuration = configuration;
        this.dataDirectory = dataDirectory;
        this.webRoot = resolvePath(dataDirectory, webpath).toPath().toAbsolutePath().normalize();
        this.normalizedWebRoot = webRoot.normalize();
        this.realWebRoot = Files.exists(webRoot) ? webRoot.toRealPath() : webRoot;
        this.allowSymlinks = configuration.getBoolean("allow-symlinks", true);
    }

    public void start() throws IOException {
        if (configuration.getBoolean("disable-webserver", true)) {
            Log.info("Internal webserver is disabled");
            return;
        }
        if (!Files.isDirectory(webRoot)) {
            throw new IOException("Web path is not a directory: " + webRoot);
        }

        int maxSessions = Math.max(1, configuration.getInteger("max-sessions", DEFAULT_MAX_SESSIONS));
        executor = Executors.newFixedThreadPool(maxSessions, r -> {
            Thread thread = new Thread(r, "DynmapWebServer");
            thread.setDaemon(true);
            return thread;
        });

        InetSocketAddress address = buildAddress("webserver-bindaddress", "webserver-port", DEFAULT_PORT);
        httpServer = HttpServer.create(address, maxSessions);
        configureServer(httpServer);
        httpServer.start();
        Log.info("Internal webserver started on http://" + printableHost(address) + ":" + address.getPort() + "/");

        if (configuration.getBoolean("webserver-ssl-enabled", false)) {
            try {
                InetSocketAddress sslAddress = buildAddress("webserver-ssl-bindaddress", "webserver-ssl-port", address.getPort() + 1);
                httpsServer = HttpsServer.create(sslAddress, maxSessions);
                ((HttpsServer) httpsServer).setHttpsConfigurator(new HttpsConfigurator(createSslContext()));
                configureServer(httpsServer);
                httpsServer.start();
                Log.info("Internal HTTPS webserver started on https://" + printableHost(sslAddress) + ":" + sslAddress.getPort() + "/");
            } catch (IOException | RuntimeException ex) {
                stopHttpsOnly();
                Log.severe("Failed to start internal HTTPS webserver", ex);
            }
        }
    }

    public void stop() {
        if (httpsServer != null) {
            httpsServer.stop(0);
            httpsServer = null;
        }
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void configureServer(HttpServer server) {
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(executor);
    }

    private SSLContext createSslContext() throws IOException {
        String keyStorePath = configuration.getString("webserver-ssl-keystore", null);
        String keyStorePassword = configuration.getString("webserver-ssl-keystore-password", "");
        String keyPassword = configuration.getString("webserver-ssl-key-password", keyStorePassword);
        String keyStoreType = configuration.getString("webserver-ssl-keystore-type", KeyStore.getDefaultType());

        if (keyStorePath == null || keyStorePath.isBlank()) {
            throw new IOException("webserver-ssl-keystore must be set when webserver-ssl-enabled is true");
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            try (InputStream in = Files.newInputStream(resolvePath(dataDirectory, keyStorePath).toPath(), StandardOpenOption.READ)) {
                keyStore.load(in, keyStorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance(configuration.getString("webserver-ssl-context", "TLS"));
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            return sslContext;
        } catch (Exception ex) {
            throw new IOException("Could not initialize webserver SSL context", ex);
        }
    }

    private InetSocketAddress buildAddress(String bindKey, String portKey, int defaultPort) throws IOException {
        String bindAddress = configuration.getString(bindKey, configuration.getString("webserver-bindaddress", null));
        int port = configuration.getInteger(portKey, defaultPort);
        if (bindAddress == null || bindAddress.isBlank()) {
            return new InetSocketAddress(port);
        }
        return new InetSocketAddress(InetAddress.getByName(bindAddress), port);
    }

    private String printableHost(InetSocketAddress address) {
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null || inetAddress.isAnyLocalAddress()) {
            return "localhost";
        }
        return inetAddress.getHostAddress();
    }

    private void stopHttpsOnly() {
        if (httpsServer != null) {
            httpsServer.stop(0);
            httpsServer = null;
        }
    }

    private static File resolvePath(File parent, String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(parent, path);
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equals(exchange.getRequestMethod()) && !"HEAD".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().set("Allow", "GET, HEAD");
                    send(exchange, 405, "Method Not Allowed");
                    return;
                }

                if (handleStorageRequest(exchange)) {
                    return;
                }

                Path file = resolveRequestPath(exchange.getRequestURI());
                if (file == null || !Files.isRegularFile(file) || !Files.isReadable(file)) {
                    send(exchange, 404, "Not Found");
                    return;
                }

                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", contentType(file));
                headers.set("X-Content-Type-Options", "nosniff");
                long length = Files.size(file);
                exchange.sendResponseHeaders(200, "HEAD".equals(exchange.getRequestMethod()) ? -1 : length);
                if (!"HEAD".equals(exchange.getRequestMethod())) {
                    try (OutputStream out = exchange.getResponseBody(); InputStream in = Files.newInputStream(file, StandardOpenOption.READ)) {
                        in.transferTo(out);
                    }
                } else {
                    exchange.close();
                }
            } catch (IllegalArgumentException ex) {
                send(exchange, 400, "Bad Request");
            } catch (IOException ex) {
                send(exchange, 500, "Internal Server Error");
            } finally {
                exchange.close();
            }
        }

        private boolean handleStorageRequest(HttpExchange exchange) throws IOException {
            if (!configuration.getBoolean("webserver-storage-endpoints", true)) {
                return false;
            }

            String requestPath = decodePath(exchange.getRequestURI().getRawPath());
            if (requestPath == null || requestPath.indexOf('\0') >= 0) {
                send(exchange, 400, "Bad Request");
                return true;
            }

            if (requestPath.equals("/up/configuration")) {
                sendStandaloneFile(exchange, "dynmap_config.json");
                return true;
            }
            if (requestPath.startsWith("/up/world/")) {
                String[] parts = requestPath.substring("/up/world/".length()).split("/");
                if (parts.length < 1 || !isSafePathPart(parts[0])) {
                    send(exchange, 400, "Bad Request");
                    return true;
                }
                sendStandaloneFile(exchange, "dynmap_" + parts[0] + ".json");
                return true;
            }
            if (requestPath.startsWith("/storage/tiles/")) {
                sendStorageTile(exchange, requestPath.substring("/storage/tiles/".length()));
                return true;
            }
            if (requestPath.startsWith("/storage/markers/")) {
                sendStorageMarker(exchange, requestPath.substring("/storage/markers/".length()));
                return true;
            }
            return false;
        }

        private void sendStandaloneFile(HttpExchange exchange, String fileid) throws IOException {
            MapStorage storage = core.getDefaultMapStorage();
            if (storage == null) {
                send(exchange, 503, "Storage Unavailable");
                return;
            }
            BufferInputStream content = storage.getStandaloneFile(fileid);
            if (content == null) {
                send(exchange, 404, "Not Found");
                return;
            }
            sendBuffer(exchange, 200, "application/json; charset=utf-8", content, -1, null);
        }

        private void sendStorageTile(HttpExchange exchange, String tilePath) throws IOException {
            String normalized = normalizeStoragePath(tilePath);
            if (normalized == null) {
                send(exchange, 400, "Bad Request");
                return;
            }

            int slash = normalized.indexOf('/');
            if (slash <= 0 || slash + 1 >= normalized.length()) {
                send(exchange, 404, "Not Found");
                return;
            }

            DynmapWorld world = getWorld(normalized.substring(0, slash));
            if (world == null) {
                send(exchange, 404, "Not Found");
                return;
            }

            MapStorage storage = core.getDefaultMapStorage();
            if (storage == null) {
                send(exchange, 503, "Storage Unavailable");
                return;
            }
            MapStorageTile tile = storage.getTile(world, normalized.substring(slash + 1));
            if (tile == null) {
                sendBlankTile(exchange);
                return;
            }

            MapStorageTile.TileRead read = tile.read();
            if (read == null || read.image == null) {
                sendBlankTile(exchange);
                return;
            }
            String contentType = read.format != null ? read.format.getContentType() : "application/octet-stream";
            sendBuffer(exchange, 200, contentType, read.image, read.lastModified, read.hashCode >= 0 ? Long.toHexString(read.hashCode) : null);
        }

        private void sendStorageMarker(HttpExchange exchange, String markerPath) throws IOException {
            String normalized = normalizeStoragePath(markerPath);
            if (normalized == null) {
                send(exchange, 400, "Bad Request");
                return;
            }

            MapStorage storage = core.getDefaultMapStorage();
            if (storage == null) {
                send(exchange, 503, "Storage Unavailable");
                return;
            }

            if (normalized.startsWith("_markers_/marker_") && normalized.endsWith(".json")) {
                String world = normalized.substring("_markers_/marker_".length(), normalized.length() - ".json".length());
                if (!isSafePathPart(world)) {
                    send(exchange, 400, "Bad Request");
                    return;
                }
                String content = storage.getMarkerFile(world);
                if (content == null) {
                    content = "{ }";
                }
                sendBytes(exchange, 200, "application/json; charset=utf-8", content.getBytes(StandardCharsets.UTF_8), -1, null);
                return;
            }

            if (normalized.startsWith("_markers_/") && normalized.endsWith(".png")) {
                String markerid = normalized.substring("_markers_/".length(), normalized.length() - ".png".length());
                if (!isSafeMarkerId(markerid)) {
                    send(exchange, 400, "Bad Request");
                    return;
                }
                BufferInputStream image = storage.getMarkerImage(markerid);
                if (image == null) {
                    sendBlankTile(exchange);
                    return;
                }
                sendBuffer(exchange, 200, "image/png", image, -1, null);
                return;
            }

            if (normalized.startsWith("faces/") && normalized.endsWith(".png")) {
                String[] parts = normalized.split("/");
                if (parts.length != 3) {
                    send(exchange, 404, "Not Found");
                    return;
                }
                PlayerFaces.FaceType faceType = PlayerFaces.FaceType.byID(parts[1]);
                String playerName = parts[2].substring(0, parts[2].length() - ".png".length());
                if (faceType == null || !isSafePathPart(playerName)) {
                    send(exchange, 400, "Bad Request");
                    return;
                }
                BufferInputStream image = storage.getPlayerFaceImage(playerName, faceType);
                if (image == null) {
                    sendBlankTile(exchange);
                    return;
                }
                sendBuffer(exchange, 200, "image/png", image, -1, null);
                return;
            }

            send(exchange, 404, "Not Found");
        }

        private DynmapWorld getWorld(String worldName) {
            if (core.mapManager == null) {
                return null;
            }
            return core.mapManager.getWorld(worldName);
        }

        private String normalizeStoragePath(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.indexOf('\0') >= 0 || path.contains("..") || path.contains("\\")) {
                return null;
            }
            return path;
        }

        private boolean isSafePathPart(String value) {
            return value != null && !value.isEmpty() && value.indexOf('/') < 0 && value.indexOf('\\') < 0 && !value.contains("..");
        }

        private boolean isSafeMarkerId(String value) {
            return value != null && !value.isEmpty() && value.indexOf('/') < 0 && value.indexOf('\\') < 0 && !value.contains("..");
        }

        private void sendBlankTile(HttpExchange exchange) throws IOException {
            Path blank = normalizedWebRoot.resolve("images/blank.png").normalize();
            if (blank.startsWith(normalizedWebRoot) && Files.isRegularFile(blank) && Files.isReadable(blank)) {
                sendBytes(exchange, 200, "image/png", Files.readAllBytes(blank), Files.getLastModifiedTime(blank).toMillis(), null);
            } else {
                send(exchange, 404, "Not Found");
            }
        }

        private Path resolveRequestPath(URI uri) throws IOException {
            String requestPath = decodePath(uri.getRawPath());
            if (requestPath == null || requestPath.indexOf('\0') >= 0) {
                return null;
            }
            if (requestPath.equals("/") || requestPath.isEmpty()) {
                requestPath = "/index.html";
            }
            if (requestPath.endsWith("/")) {
                requestPath = requestPath + "index.html";
            }

            while (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }
            Path normalized = normalizedWebRoot.resolve(requestPath).normalize();
            if (!normalized.startsWith(normalizedWebRoot)) {
                return null;
            }
            if (!allowSymlinks && Files.exists(normalized)) {
                Path real = normalized.toRealPath();
                if (!real.startsWith(realWebRoot)) {
                    return null;
                }
                return real;
            }
            return normalized;
        }

        private String decodePath(String rawPath) {
            if (rawPath == null) {
                return null;
            }
            byte[] bytes = new byte[rawPath.length()];
            int count = 0;
            for (int i = 0; i < rawPath.length(); i++) {
                char ch = rawPath.charAt(i);
                if (ch == '%' && i + 2 < rawPath.length()) {
                    if (rawPath.charAt(i + 1) == 'u' && i + 5 < rawPath.length()) {
                        int value = 0;
                        for (int j = 2; j < 6; j++) {
                            int digit = Character.digit(rawPath.charAt(i + j), 16);
                            if (digit < 0) {
                                throw new IllegalArgumentException("Invalid encoded path");
                            }
                            value = (value << 4) + digit;
                        }
                        byte[] encoded = String.valueOf((char) value).getBytes(StandardCharsets.UTF_8);
                        System.arraycopy(encoded, 0, bytes, count, encoded.length);
                        count += encoded.length;
                        i += 5;
                        continue;
                    }
                    int hi = Character.digit(rawPath.charAt(i + 1), 16);
                    int lo = Character.digit(rawPath.charAt(i + 2), 16);
                    if (hi < 0 || lo < 0) {
                        throw new IllegalArgumentException("Invalid encoded path");
                    }
                    bytes[count++] = (byte) ((hi << 4) + lo);
                    i += 2;
                } else {
                    byte[] encoded = String.valueOf(ch).getBytes(StandardCharsets.UTF_8);
                    System.arraycopy(encoded, 0, bytes, count, encoded.length);
                    count += encoded.length;
                }
            }
            try {
                return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes, 0, count)).toString();
            } catch (CharacterCodingException ex) {
                throw new IllegalArgumentException("Invalid UTF-8 path", ex);
            }
        }

        private String contentType(Path file) {
            String name = file.getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot + 1 < name.length()) {
                String type = MIME_TYPES.get(name.substring(dot + 1).toLowerCase(Locale.ROOT));
                if (type != null) {
                    return type;
                }
            }
            try {
                String detected = Files.probeContentType(file);
                if (detected != null) {
                    return detected;
                }
            } catch (IOException ignored) {
            }
            return "application/octet-stream";
        }

        private void send(HttpExchange exchange, int status, String message) throws IOException {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            sendBytes(exchange, status, "text/plain; charset=utf-8", data, -1, null);
        }

        private void sendBuffer(HttpExchange exchange, int status, String contentType, BufferInputStream content, long lastModified, String etag) throws IOException {
            sendBytes(exchange, status, contentType, content.buffer(), content.length(), lastModified, etag);
        }

        private void sendBytes(HttpExchange exchange, int status, String contentType, byte[] data, long lastModified, String etag) throws IOException {
            sendBytes(exchange, status, contentType, data, data.length, lastModified, etag);
        }

        private void sendBytes(HttpExchange exchange, int status, String contentType, byte[] data, int length, long lastModified, String etag) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            headers.set("X-Content-Type-Options", "nosniff");
            if (lastModified >= 0) {
                headers.set("Last-Modified", java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(
                        java.time.Instant.ofEpochMilli(lastModified).atZone(java.time.ZoneOffset.UTC)));
            }
            if (etag != null) {
                headers.set("ETag", "\"" + etag + "\"");
            }
            exchange.sendResponseHeaders(status, "HEAD".equals(exchange.getRequestMethod()) ? -1 : length);
            if ("HEAD".equals(exchange.getRequestMethod())) {
                exchange.close();
                return;
            }
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data, 0, length);
            }
        }
    }
}
