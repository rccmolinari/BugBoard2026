package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Utente;


import bugboard.service.AdminService;
import bugboard.service.SessioneService;

import bugboard.dto.RegisterRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SessioneService sessioneService;

    @PostMapping("/create-user/{sid}")
    public Utente creaNuovoUtente(@PathVariable UUID sid, @RequestBody RegisterRequest request) {

        // verifichiamo la sessione 
        Utente admin = sessioneService.getUtenteBySessionId(sid);

        // controllo se utente è admin
        if (admin == null || !admin.getRole().name().equalsIgnoreCase("ADMIN")) {
                return null;
        }
        return adminService.creaNuovoUtente(request);
    }


}
