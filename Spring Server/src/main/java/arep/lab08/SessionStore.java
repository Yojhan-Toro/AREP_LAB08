package arep.lab08;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén de sesiones activas en memoria.
 *
 * ¿Por qué guardar los tokens?
 * - Para poder invalidarlos (logout).
 * - Para verificar que el token que llega en cada request es legítimo.
 * - Para saber qué usuario está detrás de cada token.
 *
 * En producción esto iría en Redis o una BD, pero para el taller
 * un ConcurrentHashMap es suficiente y seguro para multi-hilo.
 */
public class SessionStore {

    // token -> username
    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    /**
     * Crea un token aleatorio para el usuario y lo guarda.
     * @return el token generado (UUID v4)
     */
    public static String createToken(String username) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, username);
        return token;
    }

    /**
     * Valida que el token exista en la tienda de sesiones.
     */
    public static boolean isValid(String token) {
        return token != null && activeSessions.containsKey(token);
    }

    /**
     * Retorna el username asociado al token, o null si no existe.
     */
    public static String getUsername(String token) {
        return activeSessions.get(token);
    }

    /**
     * Elimina el token (logout).
     */
    public static void invalidate(String token) {
        activeSessions.remove(token);
    }
}
