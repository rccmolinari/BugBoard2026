package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.LogoutRequest;
import bugboard.dto.RegisterRequest;

import bugboard.service.AuthService;
import bugboard.service.SessioneService;

/*
 * Controller auth minimale.
 * Espone login/register sotto /auth e passa la palla al service.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Service con la logica vera di autenticazione/registrazione.
    @Autowired
    private AuthService authService;

    @Autowired
    private SessioneService sessioneService;

    // Login: prende email/password dal body e torna i dati utente per la sessione frontend.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }

        return ResponseEntity.ok(response);
    }

    // Register: crea l'utente e torna true/false in base all'esito.
    @PostMapping("/register")
    public boolean register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        if (request == null || request.getSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SessionId mancante");
        }

        sessioneService.deleteSession(request.getSessionId());
        return ResponseEntity.noContent().build();
    }
}
