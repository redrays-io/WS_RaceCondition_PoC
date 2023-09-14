package io.redrays.ws.concept.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebSocketParallel_Success {

    public static void main(String[] args) {
        // Define the WebSocket server URI
        String serverUri = "ws://127.0.0.1:8080";

        // Number of WebSocket clients to create
        int numClients = 100;

        // Create an ExecutorService to manage multiple WebSocket client threads
        ExecutorService executor = Executors.newFixedThreadPool(numClients);

        // Create a list to store WebSocket client instances
        List<WebSocketClient> clients = new ArrayList<>();

        // Loop to create and configure multiple WebSocket clients
        for (int i = 0; i < numClients; i++) {
            int clientId = i + 1;
            try {
                // Create a WebSocket client for each connection
                WebSocketClient webSocketClient = new WebSocketClient(new URI(serverUri)) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        // Handle WebSocket connection opened event
                        System.out.println("Client " + clientId + " connected to the WebSocket server");
                        this.send("Hello, WebSocket server! From client " + clientId);
                    }

                    @Override
                    public void onMessage(String message) {
                        // Handle incoming WebSocket messages
                        System.out.println("Client " + clientId + " received message: " + message);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        // Handle WebSocket connection closed event
                        System.out.println("Client " + clientId + " connection closed: " + reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        // Handle WebSocket error
                        System.out.println("Client " + clientId + " error occurred: " + ex.getMessage());
                    }
                };

                // Add the WebSocket client to the list
                clients.add(webSocketClient);

                // Connect the WebSocket client in a separate thread
                executor.submit(webSocketClient::connect);

            } catch (URISyntaxException e) {
                System.out.println("Invalid WebSocket server URI: " + e.getMessage());
            }
        }

        // Shutdown the executor after all tasks are submitted
        executor.shutdown();

        // Wait for all WebSocket client threads to complete
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for tasks to complete: " + e.getMessage());
        }
    }
}
