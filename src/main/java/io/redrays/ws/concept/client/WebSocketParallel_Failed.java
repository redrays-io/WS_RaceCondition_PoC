package io.redrays.ws.concept.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebSocketParallel_Failed {

    public static void main(String[] args) {
        String serverUri = "ws://127.0.0.1:8080"; // WebSocket server URI
        int numParallelRequests = 285; // Number of parallel WebSocket requests

        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI(serverUri)) {

                // This method is called when the WebSocket connection is successfully opened.
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to the WebSocket server");

                    // Create a fixed thread pool to manage parallel requests
                    ExecutorService executor = Executors.newFixedThreadPool(numParallelRequests);

                    for (int i = 0; i < numParallelRequests; i++) {
                        int messageId = i + 1;
                        executor.submit(() -> {
                            this.send("Hello, WebSocket server! Message ID: " + messageId);
                            System.out.println("Sent message with ID: " + messageId);
                        });
                    }

                    // Shutdown the executor after all tasks are submitted
                    executor.shutdown();

                    // Wait for the tasks to complete
                    try {
                        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while waiting for tasks to complete: " + e.getMessage());
                    }
                }

                // This method is called when a WebSocket message is received.
                @Override
                public void onMessage(String message) {
                    System.out.println("Received message: " + message);
                }

                // This method is called when the WebSocket connection is closed.
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection closed: " + reason);
                }

                // This method is called when an error occurs in the WebSocket connection.
                @Override
                public void onError(Exception ex) {
                    System.out.println("Error occurred: " + ex.getMessage());
                }
            };

            // Connect to the WebSocket server
            webSocketClient.connect();

        } catch (URISyntaxException e) {
            System.out.println("Invalid WebSocket server URI: " + e.getMessage());
        }
    }
}
