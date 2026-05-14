package bugboard.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Utente;
import bugboard.service.NotifyUIService;
import bugboard.service.SessioneService;

import bugboard.dto.Notify;

import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = "*")

public class NotificaController {
    
    @Autowired
    private NotifyUIService notifyUIService;

    @Autowired
    private SessioneService sessioneService;

    /**
     * recuoera notifiche utente assegnato a sid
     */

    @GetMapping("/{sid}")
    public List<Notify> getMieNotifiche(@PathVariable UUID sid) {
        
          Utente utente = sessioneService.getUtenteBySessionId(sid);

          if(utente == null){
            return null;
          }

          return notifyUIService.getNotificaPerUtente(utente.getId());
    }
    

    /**
     * Segna una notifica come letta successivamente trigger su postgress cancella il record
     */
    @PutMapping("/leggi/{id}")
     public boolean leggiNotifica(@PathVariable int id) {
        return notifyUIService.segnaComeLetta(id);
    }
}
