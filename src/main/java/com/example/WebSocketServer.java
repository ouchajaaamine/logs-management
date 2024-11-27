package com.example;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.Gson;

// WebSocket server endpoint that handles log broadcasting
@ServerEndpoint("/logs")
public class WebSocketServer {
    private static Set<Session> sessions = new HashSet<>(); // Set of all active WebSocket sessions

    // Called when a new WebSocket connection is opened
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session); // Add the session to the active sessions set
    }

    // Called when a WebSocket connection is closed
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session); // Remove the session from the active sessions set
    }

    // Called when an error occurs on a WebSocket connection
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace(); // Print error details for debugging
    }

    // Sends logs to all connected and open WebSocket sessions
    public static void sendLogsToClients(List<LogEntry> logs) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    // Convert log entries to JSON and send to client
                    session.getBasicRemote().sendText(new Gson().toJson(logs));
                } catch (IOException e) {
                    e.printStackTrace(); // Handle any I/O errors during message sending
                }
            }
        }
    }
}
