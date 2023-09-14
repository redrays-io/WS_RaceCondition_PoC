## Race Conditions in Websockets 

In the field of information technology, ensuring data reliability and integrity is crucial, especially with millions of users and terabytes of data in the mix. However, as software systems become more complex, issues such as race conditions can arise, significantly impacting system operation and leading to unpredictable outcomes.

### What are race conditions?

Race conditions, also referred to as state races, are errors that occur in multitasking programs when two or more threads or processes attempt to modify shared data or resources simultaneously without synchronization. This can result in unexpected and unpredictable outcomes since the order of execution of operations depends on which threads or processes finish first. Detecting race conditions is a challenging issue in software systems due to their unpredictable nature.

### Sources and Further Reading:

- "Race Condition" on Wikipedia
- "Smashing the State Machine: the True Potential of Web Race Conditions" by James Kettle

While there are numerous resources available about classic race conditions on the internet, this article explores whether race conditions can occur in WebSockets.

### WebSockets: Real-Time Bidirectional Connections

WebSockets are a cutting-edge technology that significantly improves interaction in web applications by providing an open bidirectional connection between a user's web browser and a web server. This seamless connection enables data exchange without the need for constantly initiating new HTTP requests, making them ideal for creating interactive applications.

### Advantages of WebSockets:

1. Low Latency: WebSockets minimize data exchange delays, making them an excellent choice for applications where real-time matters, such as online games or chat applications.
2. Resource Efficiency: WebSockets support a persistent connection, reducing the overhead associated with establishing and tearing down connections compared to numerous consecutive HTTP requests.
3. Bidirectional Data Exchange: Both the client and server can send data to each other without waiting for a request, making WebSockets an excellent tool for creating interactive web applications.
4. Compatibility: WebSockets can be used in various programming languages and technologies, making them a universal tool for creating interactive web applications.

To demonstrate the concept, this article includes a Java code that represents a WebSocket server that interacts with a PostgreSQL database. The server uses the Java-WebSocket library to handle WebSocket connections and performs the following tasks:

1. After launching the program, the Java code connects to the database and checks if the "example" table exists. If it doesn't exist, it creates the table and inserts random data:
   - RandomName0
   - RandomName1
   - ...
   - RandomName9

2. The most interesting code is found in the "onMessage" function. 
```java
public static int a = 0;

@Override
public void onMessage(WebSocket conn, String message) {
    if (a == 0) {
        try {
            // some activity with db
            int rowCount = getCountFromExampleTable();

        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
        conn.send("Echo: " + message);
        a = a + 1;
        System.out.println(a);
    }
}

```
There's a global variable "id" initialized to 0. When a client connects to the server and sends a message, it checks if "id == 0", indicating whether this function has already been executed. If not, a simple SQL command is executed to select the count of rows from the "example" table. Then, "id" is incremented by 1, and its value is printed. And in theory, the function should not be executed 2 times. 

In regards to the client, two types of clients have been created: "WebSocketParallel_Success" 
```java
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

```

and "WebSocketParallel_Failed"

```java
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

```

. Attempts have been made to create a race condition using two different methods. In the first file, multiple connections are created in parallel, and data is sent to the server, while in the second file, only one connection is established, but data is sent one after the other. 

As evident from the class names, if multiple connections are created in parallel and data is sent, the race condition will occur. However, it won't occur in the second case since WebSockets transmit data sequentially in a single connection.

As shown in the screenshots below, race conditions can occur.
![image](https://github.com/vah13/WS_RaceCondition_PoC/assets/7976421/e9f094d5-4981-4159-b5c6-ed789b602b34)

![image](https://github.com/vah13/WS_RaceCondition_PoC/blob/master/WS_RaceCondition_demo.gif)


Original demo video here https://redrays.io/wp-content/uploads/2023/09/WS_RaceCondition_demo_video.mp4
### Are there any race condition vulnerabilities in WebSockets in the wild?

Yes, a few critical race condition vulnerabilities have been detected in some cryptocurrency exchanges using WS API. These issues have been reported, and the exchanges subsequently fixed them.
