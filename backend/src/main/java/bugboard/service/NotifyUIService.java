package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Notifica;
import bugboard.repository.NotificaRepository;

import bugboard.dto.Notify;

import java.util.List;
import java.util.Optional;




@Service
public class NotifyUIService implements NotifyService {
    @Override
    public void setNotificationRead(int notificationId) {
        // Logica per segnare una notifica come letta nell'interfaccia utente
        this.segnaComeLetta(notificationId);
    }
    

    @Autowired
    private NotificaRepository notificaRepository;

    public List<Notify> getNotificaPerUtente(Integer utenteId) {
      
        List<Notifica> notifica = notificaRepository.findByAssegnatoAId(utenteId);
          
        //Convertiamo notifica in dto
        return notifica.stream().map(n -> {
            Notify dto = new Notify();
            dto.setId(n.getId());
            dto.setDataCreazione(n.getDataCreazione());
            dto.setLetta(n.isLetta());
            if(n.getIssue() != null) {
                dto.setIssueId(n.getIssue().getId());
                dto.setTitoloIssue(n.getIssue().getTitolo());
                
                String titolo = n.getIssue().getTitolo();
                dto.setMessaggio("Ti è stata assegnata la issue  '" + titolo + "'");
            
            } else {
                dto.setMessaggio("Nuova notifica dal sistema");
            }
        
            return dto;

      }).toList();
    }


    @Transactional
    public  boolean segnaComeLetta(int id) {
        Optional<Notifica> optNotifica = notificaRepository.findById(id);

        if(optNotifica.isPresent()) {
            Notifica n = optNotifica.get();
            n.setLetta(true);
            notificaRepository.save(n);
            return true;
        }
        return false;
    }

    public int contaNotificheNonLette(Integer utenteId) {
        return notificaRepository.countByAssegnatoAIdAndLettaFalse(utenteId);
    }


    /**
     * recupera l'id  dell'issue associata a una notifica ci serve a sapere quando utente ci clicca
     */
    public Integer getIssueDaNotifica(int id) {
        Optional<Notifica> opt  = notificaRepository.findById(id);
        if (opt.isPresent()) {
            Notifica n = opt.get();
            if(n.getIssue() != null) {
                return n.getIssue().getId();
            } else {
                return null;
            } 
        } else {
                return null;
        }
    }
    
}
