package arep.lab08;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio en memoria de usuarios.
 * Las contraseñas se almacenan como hashes BCrypt — nunca en texto plano.
 *
 * Para generar un hash nuevo puedes correr:
 *   new BCryptPasswordEncoder().encode("tu-contraseña")
 */
public class UserRepository {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // username -> hash BCrypt de la contraseña
    private static final Map<String, String> users = new HashMap<>();

    static {
        // Contraseña real: "admin123"
        users.put("admin", encoder.encode("admin123"));
        // Contraseña real: "user123"
        users.put("user", encoder.encode("user123"));
    }

    /**
     * Verifica si el usuario existe y la contraseña es correcta.
     * BCrypt compara el texto plano contra el hash almacenado.
     */
    public static boolean validate(String username, String rawPassword) {
        String storedHash = users.get(username);
        if (storedHash == null) return false;
        return encoder.matches(rawPassword, storedHash);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }
}
