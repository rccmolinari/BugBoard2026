package bugboard.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Notifica;
import bugboard.model.Utente;
import bugboard.service.NotificaService;
import bugboard.service.SessioneService;

import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = "*")

public class NotificaController {
    
    @Autowired
    private NotificaService notificaService;

    @Autowired
    private SessioneService sessioneService;

    /**
     * recuoera notifiche utente assegnato a sid
     */

    @GetMapping("/{sid}")
    public ResponseEntity<List<Notifica>> getMieNotifiche(@PathVariable UUID sid) {
        
          Utente utente =sessioneService.getUtenteBySessionId(sid);

          if(utente == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
          }

          List<Notifica> notifica = notificaService.getNoticaPerUtente(utente.getId());
          return new ResponseEntity<>(notifica, HttpStatus.OK);
    }
    

    /**
     * Segna una notifica come letta successivamente trigger su postgress cancella il record
     */
    @PutMapping("/leggi/{id}")
     public ResponseEntity<Void> leggiNotifica(@PathVariable int id) {
        boolean success = notificaService.segnaComeLetta(id);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
