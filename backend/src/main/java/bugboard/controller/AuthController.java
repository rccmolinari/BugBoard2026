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

import bugboard.service.IAuthService;
import bugboard.service.ISessioneService;

/*
 * DIP — inietta IAuthService e ISessioneService (astrazioni),
 * non le classi concrete AuthService / SessioneService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @Autowired
    private ISessioneService sessioneService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }

        return ResponseEntity.ok(response);
    }

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
