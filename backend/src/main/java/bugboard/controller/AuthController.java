package bugboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;

import bugboard.service.IAuthService;
import bugboard.service.ISessioneService;

import java.util.UUID;

/*
 * Le tre rotte dell'autenticazione: login, registrazione e logout. Come nel
 * resto delle API, anche il logout si porta l'id di sessione nell'header
 * X-Session-Id e non nell'URL o nel body.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String SID_HEADER = "X-Session-Id";

    private final IAuthService authService;
    private final ISessioneService sessioneService;

    public AuthController(IAuthService authService, ISessioneService sessioneService) {
        this.authService = authService;
        this.sessioneService = sessioneService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        if (sid != null) {
            sessioneService.deleteSession(sid);
        }
        return ResponseEntity.noContent().build();
    }
}
