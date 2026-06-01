package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Utente;
import bugboard.service.IUserService;
import bugboard.dto.AllUserResponse;
import bugboard.dto.RegisterRequest;

import java.util.List;
import java.util.UUID;

/*
 * DIP — inietta IUserService (astrazione), non UserService direttamente.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/create/{sid}")
    public Utente creaNuovoUtente(@PathVariable UUID sid, @RequestBody RegisterRequest request) {
        return userService.creaNuovoUtente(sid, request);
    }

    @GetMapping("/all/{sid}")
    public List<AllUserResponse> getAllUsers(@PathVariable UUID sid) {
        return userService.getAllUsers(sid);
    }
}
