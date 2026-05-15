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
@RequestMapping("/api/notifies")
@CrossOrigin(origins = "*")

public class NotificaController {
    
    @Autowired
    private NotifyUIService notifyUIService;

    @Autowired
    private SessioneService sessioneService;

    /**
     * recuoera numero notifiche non lette per   utente assegnato a sid
     */

    @GetMapping("/number/{sid}")
    public int countIssues(@PathVariable UUID sid) {
        
          Utente utente = sessioneService.getUtenteBySessionId(sid);

          if(utente == null){
            return 0;
          }

          return notifyUIService.contaNotificheNonLette(utente.getId());
    }

    @GetMapping("/list/{sid}")
    public List<Notify> getMieNotifiche(@PathVariable UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) {
            return List.of();
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
