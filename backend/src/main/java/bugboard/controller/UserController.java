package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Utente;


import bugboard.service.UserService;
import bugboard.service.SessioneService;
import bugboard.dto.AllUserResponse;
import java.util.List;
import bugboard.dto.RegisterRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {  
    @Autowired
    private UserService userService;

    @PostMapping("/create/{sid}")
    public Utente creaNuovoUtente(@PathVariable UUID sid, @RequestBody RegisterRequest request) {


        return userService.creaNuovoUtente(sid, request);
    }

    @GetMapping("/all/{sid}")
    public List<AllUserResponse> getAllUsers(@PathVariable UUID sid) {
        // verifichiamo la sessione 
        return userService.getAllUsers(sid);
    }


}
