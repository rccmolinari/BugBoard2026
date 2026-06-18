package bugboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bugboard.service.IUserService;
import bugboard.dto.AllUserResponse;
import bugboard.dto.RegisterRequest;

import java.util.List;
import java.util.UUID;

/*
 * DIP — inietta IUserService (astrazione) via costruttore.
 * La sessione arriva dall'header X-Session-Id; i fallimenti (permessi,
 * utente inesistente, ...) diventano status HTTP via GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final String SID_HEADER = "X-Session-Id";

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<AllUserResponse> creaNuovoUtente(@RequestHeader(value = SID_HEADER, required = false) UUID sid,
                                                           @RequestBody RegisterRequest request) {
        AllUserResponse created = userService.creaNuovoUtente(sid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public List<AllUserResponse> getAllUsers(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return userService.getAllUsers(sid);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Void> deleteUser(@RequestHeader(value = SID_HEADER, required = false) UUID sid,
                                           @PathVariable String email) {
        userService.deleteUser(sid, email);
        return ResponseEntity.noContent().build();
    }
}
