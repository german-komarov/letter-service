package com.german.letterservice.util.holders;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketSessionHolder {
    static {
        sessions = new HashMap<>();
    }

    private static final Map<String, List<WebSocketSession>> sessions;

    public static void addSession(String username, WebSocketSession session)
    {
        synchronized (sessions) {
            List<WebSocketSession> userSessions = sessions.get(username);
            if (userSessions == null)
                userSessions = new ArrayList<>();

            userSessions.add(session);
            sessions.put(username, userSessions);
        }
    }

    public static void closeSessions(String username) throws IOException
    {
        synchronized (sessions) {
            List<WebSocketSession> userSessions = sessions.get(username);
            if (userSessions != null)
            {
                for(WebSocketSession session : userSessions) {
                    session.close();
                }
                sessions.remove(username);
            }
        }
    }

}
