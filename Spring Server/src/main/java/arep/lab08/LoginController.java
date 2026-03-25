package arep.lab08;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticación.
 *
 * Endpoints:
 *  POST /api/login   -> recibe {username, password}, retorna {token} o 401
 *  GET  /api/me      -> verifica token en header, retorna {username}
 *  POST /api/logout  -> invalida el token
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    // DTO de entrada para el login
    public static class LoginRequest {
        public String username;
        public String password;
    }

    /**
     * Login: valida credenciales y retorna un token de sesión.
     *
     * Ejemplo de request:
     *   POST /api/login
     *   Content-Type: application/json
     *   {"username": "admin", "password": "admin123"}
     *
     * Respuesta exitosa (200):
     *   {"token": "uuid-generado", "username": "admin"}
     *
     * Respuesta fallida (401):
     *   {"error": "Credenciales inválidas"}
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        if (req.username == null || req.password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username y password son requeridos"));
        }

        boolean valid = UserRepository.validate(req.username, req.password);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        String token = SessionStore.createToken(req.username);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", req.username
        ));
    }

    /**
     * Verifica quién está autenticado según el token en el header.
     * Header esperado: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);

        if (!SessionStore.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido o expirado"));
        }

        String username = SessionStore.getUsername(token);
        return ResponseEntity.ok(Map.of("username", username));
    }

    /**
     * Logout: invalida el token actual.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(authHeader);
        SessionStore.invalidate(token);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    // Extrae el token del header "Bearer <token>"
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
