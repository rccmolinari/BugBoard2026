package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.service.AuthService;
import bugboard.dto.RegisterRequest;

/*
 * Controller auth minimale.
 * Espone login/register sotto /api e passa la palla al service.
 */
@RestController
@RequestMapping("/api")
public class TestController {

    // Service con la logica vera di autenticazione/registrazione.
    @Autowired
    private AuthService authService;

    // Login: prende email/password dal body e torna i dati utente per la sessione frontend.
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        return authService.login(email, password);
    }

    // Register: crea l'utente e torna true/false in base all'esito.
    @PostMapping("/register")
    public boolean register(@RequestBody RegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String name = request.getName();
        String surname = request.getSurname();
        return authService.register(email, password, name, surname);
    }
}
