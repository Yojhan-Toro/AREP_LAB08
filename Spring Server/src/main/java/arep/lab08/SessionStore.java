package arep.lab08;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {

    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public static String createToken(String username) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, username);
        return token;
    }

    public static boolean isValid(String token) {
        return token != null && activeSessions.containsKey(token);
    }

    public static String getUsername(String token) {
        return activeSessions.get(token);
    }

    public static void invalidate(String token) {
        activeSessions.remove(token);
    }
}
