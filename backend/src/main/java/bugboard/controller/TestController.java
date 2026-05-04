package bugboard.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import bugboard.repository.UtenteRepository;

@RestController
public class TestController {

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/test-db")
    public String testConnessione() {
        try {
            long count = utenteRepository.count();
            return "Connessione riuscita! Nel database ci sono " + count + " utenti.";
        } catch (Exception e) {
            return "Errore di connessione: " + e.getMessage();
        }
    }
} 
