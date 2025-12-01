package com.api.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket utility class for managing WebSocket connections and message handling
 */
@Slf4j
public class WebSocketUtil {

    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 30000; // 30 seconds
    private static final Map<String, WebSocketConnection> connectionPool = new ConcurrentHashMap<>();
    private static final AtomicInteger connectionIdGenerator = new AtomicInteger(0);

    /**
     * WebSocket connection wrapper
     */
    public static class WebSocketConnection {
        private final String connectionId;
        private final WebSocketClient client;
        private final BlockingQueue<String> messageQueue;
        private final List<String> messageHistory;
        private final Map<String, CompletableFuture<JSONObject>> pendingRequests;
        private ScheduledExecutorService heartbeatExecutor;
        private volatile boolean connected = false;

        public WebSocketConnection(String url, Map<String, String> headers) throws Exception {
            this.connectionId = "ws-conn-" + connectionIdGenerator.incrementAndGet();
            this.messageQueue = new LinkedBlockingQueue<>();
            this.messageHistory = Collections.synchronizedList(new ArrayList<>());
            this.pendingRequests = new ConcurrentHashMap<>();

            URI uri = new URI(url);
            this.client = createWebSocketClient(uri, headers);

            // Enable SSL for wss://
            if (url.startsWith("wss://")) {
                SSLContext sslContext = createTrustAllSSLContext();
                SSLSocketFactory factory = sslContext.getSocketFactory();
                client.setSocketFactory(factory);
            }
        }

        private WebSocketClient createWebSocketClient(URI uri, Map<String, String> headers) {
            return new WebSocketClient(uri, headers) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    log.info("[{}] WebSocket connection opened: {}", connectionId, uri);
                }

                @Override
                public void onMessage(String message) {
                    log.info("[{}] Received message: {}", connectionId, message);
                    messageQueue.offer(message);
                    messageHistory.add(message);

                    // Handle response for pending requests
                    try {
                        JSONObject jsonMessage = JSON.parseObject(message);
                        String id = jsonMessage.getString("id");
                        if (id != null && pendingRequests.containsKey(id)) {
                            pendingRequests.get(id).complete(jsonMessage);
                        }
                    } catch (Exception e) {
                        log.debug("Message is not a JSON response: {}", message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    log.info("[{}] WebSocket connection closed: code={}, reason={}", connectionId, code, reason);
                    stopHeartbeat();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("[{}] WebSocket error: {}", connectionId, ex.getMessage(), ex);
                }
            };
        }

        public boolean connect(int timeout) throws InterruptedException {
            client.connectBlocking(timeout, TimeUnit.MILLISECONDS);
            return client.isOpen();
        }

        public void sendMessage(String message) {
            if (client.isOpen()) {
                client.send(message);
                log.info("[{}] Sent message: {}", connectionId, message);
            } else {
                throw new IllegalStateException("WebSocket connection is not open");
            }
        }

        public void sendJsonMessage(JSONObject message) {
            sendMessage(message.toJSONString());
        }

        public String waitForMessage(long timeout) throws InterruptedException {
            return messageQueue.poll(timeout, TimeUnit.MILLISECONDS);
        }

        public List<String> waitForMessages(int count, long timeout) throws InterruptedException {
            List<String> messages = new ArrayList<>();
            long endTime = System.currentTimeMillis() + timeout;

            while (messages.size() < count && System.currentTimeMillis() < endTime) {
                long remainingTime = endTime - System.currentTimeMillis();
                String message = messageQueue.poll(remainingTime, TimeUnit.MILLISECONDS);
                if (message != null) {
                    messages.add(message);
                }
            }

            return messages;
        }

        public CompletableFuture<JSONObject> sendRequestAndWaitForResponse(JSONObject request, long timeout) {
            String id = request.getString("id");
            if (id == null) {
                id = String.valueOf(System.currentTimeMillis());
                request.put("id", id);
            }

            final String finalId = id; // Make id final for lambda
            CompletableFuture<JSONObject> future = new CompletableFuture<>();
            pendingRequests.put(finalId, future);

            sendJsonMessage(request);

            // Set timeout for the future
            CompletableFuture.delayedExecutor(timeout, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (!future.isDone()) {
                        future.completeExceptionally(new TimeoutException("Request timeout after " + timeout + "ms"));
                        pendingRequests.remove(finalId);
                    }
                });

            return future;
        }

        public void startHeartbeat(JSONObject heartbeatMessage, int interval) {
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdown();
            }

            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (client.isOpen()) {
                        sendJsonMessage(heartbeatMessage);
                        log.debug("[{}] Heartbeat sent", connectionId);
                    }
                } catch (Exception e) {
                    log.error("[{}] Failed to send heartbeat: {}", connectionId, e.getMessage());
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
        }

        public void stopHeartbeat() {
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdown();
                heartbeatExecutor = null;
            }
        }

        public void close() {
            stopHeartbeat();
            if (client != null) {
                client.close();
            }
        }

        public boolean isConnected() {
            return connected && client.isOpen();
        }

        public String getConnectionId() {
            return connectionId;
        }

        public List<String> getMessageHistory() {
            return new ArrayList<>(messageHistory);
        }

        public void clearMessageQueue() {
            messageQueue.clear();
        }
    }

    /**
     * Create a new WebSocket connection
     */
    public static WebSocketConnection createConnection(String url, Map<String, String> headers) throws Exception {
        WebSocketConnection connection = new WebSocketConnection(url, headers);
        if (connection.connect(DEFAULT_TIMEOUT)) {
            connectionPool.put(connection.getConnectionId(), connection);
            return connection;
        } else {
            throw new RuntimeException("Failed to establish WebSocket connection to " + url);
        }
    }

    /**
     * Create a new WebSocket connection without headers
     */
    public static WebSocketConnection createConnection(String url) throws Exception {
        return createConnection(url, new HashMap<>());
    }

    /**
     * Create multiple WebSocket connections
     */
    public static List<WebSocketConnection> createConnections(int count, String url, Map<String, String> headers) throws Exception {
        List<WebSocketConnection> connections = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            connections.add(createConnection(url, headers));
            Thread.sleep(100); // Small delay between connections
        }
        return connections;
    }

    /**
     * Get connection by ID
     */
    public static WebSocketConnection getConnection(String connectionId) {
        return connectionPool.get(connectionId);
    }

    /**
     * Get all active connections
     */
    public static Collection<WebSocketConnection> getAllConnections() {
        return connectionPool.values();
    }

    /**
     * Close connection by ID
     */
    public static void closeConnection(String connectionId) {
        WebSocketConnection connection = connectionPool.remove(connectionId);
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Close all connections
     */
    public static void closeAllConnections() {
        for (WebSocketConnection connection : connectionPool.values()) {
            connection.close();
        }
        connectionPool.clear();
    }

    /**
     * Get active connection count
     */
    public static int getActiveConnectionCount() {
        return (int) connectionPool.values().stream()
            .filter(WebSocketConnection::isConnected)
            .count();
    }

    /**
     * Create SSL context that trusts all certificates (for testing only!)
     */
    private static SSLContext createTrustAllSSLContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * Test connection limit by creating multiple connections
     */
    public static Map<String, Object> testConnectionLimit(String url, int maxConnections, Map<String, String> headers) {
        Map<String, Object> result = new HashMap<>();
        List<WebSocketConnection> connections = new ArrayList<>();

        try {
            // Try to create max connections
            for (int i = 0; i < maxConnections; i++) {
                WebSocketConnection conn = createConnection(url, headers);
                connections.add(conn);
                log.info("Successfully created connection #{}", i + 1);
            }

            result.put("maxConnectionsReached", true);
            result.put("connectedCount", connections.size());

            // Try to create one more connection (should fail or succeed based on limit)
            try {
                WebSocketConnection extraConn = createConnection(url, headers);
                connections.add(extraConn);
                result.put("exceedLimitSuccess", true);
                result.put("totalConnections", connections.size());
            } catch (Exception e) {
                result.put("exceedLimitSuccess", false);
                result.put("exceedLimitError", e.getMessage());
            }

            // Test closing one and creating new one
            if (!connections.isEmpty()) {
                WebSocketConnection firstConn = connections.get(0);
                closeConnection(firstConn.getConnectionId());
                connections.remove(0);

                try {
                    WebSocketConnection newConn = createConnection(url, headers);
                    connections.add(newConn);
                    result.put("reconnectAfterCloseSuccess", true);
                } catch (Exception e) {
                    result.put("reconnectAfterCloseSuccess", false);
                    result.put("reconnectError", e.getMessage());
                }
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("connectedCount", connections.size());
        } finally {
            // Clean up all connections
            for (WebSocketConnection conn : connections) {
                closeConnection(conn.getConnectionId());
            }
        }

        return result;
    }
}