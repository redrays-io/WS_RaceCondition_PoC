package io.redrays.ws.concept.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.sql.ResultSet;

public class EchoWebSocketServer extends WebSocketServer {

    // PostgreSQL JDBC URL and driver class name
    private static final String POSTGRESQL_JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";
    private static final String POSTGRESQL_USERNAME = "postgres";
    private static final String POSTGRESQL_PASSWORD = "passw";

    public EchoWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    // Operation: Handle client connection
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //System.out.println("New client connected: " + conn.getRemoteSocketAddress());
    }

    // Operation: Handle client disconnection
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress() + " Reason: " + reason);
    }

    public static int id = 0;

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (id == 0) {
            try {
                // some activity with db
                int rowCount = getCountFromExampleTable();
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
            }
            conn.send("Echo: " + message);
            id = id + 1;
            System.out.println(id);
        }
    }

    /**
     * Execute a 'SELECT COUNT(*) FROM example' query and return the result.
     *
     * @return The count from the 'example' table.
     * @throws SQLException if a database error occurs during the query.
     */
    private int getCountFromExampleTable() throws SQLException {
        int count = 0;
        try (Connection connection = DriverManager.getConnection(POSTGRESQL_JDBC_URL, POSTGRESQL_USERNAME, POSTGRESQL_PASSWORD);
             Statement statement = connection.createStatement()) {
            String query = "SELECT COUNT(*) FROM example";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        }
        return count;
    }

    // Operation: Handle WebSocket error
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Error occurred: " + ex.getMessage());
    }

    // Operation: Handle WebSocket server startup
    @Override
    public void onStart() {
        System.out.println("WebSocket server started on " + getAddress());

        // Create the table and insert random data when the server starts
        try {
            createAndPopulateTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a table "example" (id serial primary key, name text) if it doesn't exist.
     *
     * @throws SQLException if a database error occurs during table creation.
     */
    private void createTableIfNotExists(Connection connection) throws SQLException {
        try (Statement createTableStatement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS example (id serial primary key, name text)";
            createTableStatement.execute(createTableQuery);
        }
    }

    /**
     * Insert 10 random records into the "example" table.
     *
     * @throws SQLException if a database error occurs during data insertion.
     */
    private void insertRandomData(Connection connection) throws SQLException {
        try (Statement insertStatement = connection.createStatement()) {
            Random random = new Random();
            for (int j = 0; j < 10; j++) {
                String name = "RandomName" + j;
                String insertQuery = "INSERT INTO example (name) VALUES ('" + name + "')";
                insertStatement.execute(insertQuery);
            }
        }
    }

    /**
     * Create the "example" table if it doesn't exist and insert random data into it.
     *
     * @throws SQLException if a database error occurs.
     */
    private void createAndPopulateTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(POSTGRESQL_JDBC_URL, POSTGRESQL_USERNAME, POSTGRESQL_PASSWORD)) {
            // Create the table if it doesn't exist
            createTableIfNotExists(connection);

            // Insert 10 random records into the table
            insertRandomData(connection);
        } catch (SQLException ex) {
            System.out.println("Error occurred while creating and populating the table: " + ex.getMessage());
        }
    }

    // Main method to start the WebSocket server
    public static void main(String[] args) {
        int port = 8080; // Set the server's port
        // Load PostgreSQL JDBC driver
        try {
            Class.forName(POSTGRESQL_JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC driver not found.");
            return;
        }
        EchoWebSocketServer server = new EchoWebSocketServer(new InetSocketAddress(port));
        server.start();
    }
}
