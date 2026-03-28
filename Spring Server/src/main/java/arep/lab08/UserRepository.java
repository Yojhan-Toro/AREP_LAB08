package arep.lab08;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRepository {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Map<String, String> users = new HashMap<>();

    static {
        users.put("admin", encoder.encode("admin123"));
        users.put("user", encoder.encode("user123"));
    }

    public static boolean validate(String username, String rawPassword) {
        String storedHash = users.get(username);
        if (storedHash == null) return false;
        return encoder.matches(rawPassword, storedHash);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }
}
