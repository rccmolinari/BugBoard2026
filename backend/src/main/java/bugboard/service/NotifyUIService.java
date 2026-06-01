package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Notifica;
import bugboard.repository.NotificaRepository;
import bugboard.dto.Notify;

import java.util.List;
import java.util.Optional;

/*
 * SRP  — gestisce solo la logica di lettura e aggiornamento delle notifiche.
 * LSP  — implementa correttamente l'intero contratto di NotifyService;
 *        qualsiasi altra implementazione può sostituirla senza che i
 *        controller lo sappiano.
 * DIP  — i controller iniettano NotifyService (interfaccia), non questa classe.
 */
@Service
public class NotifyUIService implements NotifyService {

    @Autowired
    private NotificaRepository notificaRepository;

    @Override
    public List<Notify> getNotificaPerUtente(Integer utenteId) {
        List<Notifica> notifiche = notificaRepository.findByAssegnatoAId(utenteId);

        return notifiche.stream().map(n -> {
            Notify dto = new Notify();
            dto.setId(n.getId());
            dto.setDataCreazione(n.getDataCreazione());
            dto.setLetta(n.isLetta());
            if (n.getIssue() != null) {
                dto.setIssueId(n.getIssue().getId());
                dto.setTitoloIssue(n.getIssue().getTitolo());
                dto.setMessaggio("Ti è stata assegnata la issue '" + n.getIssue().getTitolo() + "'");
            } else {
                dto.setMessaggio("Nuova notifica dal sistema");
            }
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public boolean segnaComeLetta(int id) {
        Optional<Notifica> opt = notificaRepository.findById(id);
        if (opt.isPresent()) {
            opt.get().setLetta(true);
            notificaRepository.save(opt.get());
            return true;
        }
        return false;
    }

    @Override
    public int contaNotificheNonLette(Integer utenteId) {
        return notificaRepository.countByAssegnatoAIdAndLettaFalse(utenteId);
    }

    @Override
    public Integer getIssueDaNotifica(int id) {
        return notificaRepository.findById(id)
            .map(n -> n.getIssue() != null ? n.getIssue().getId() : null)
            .orElse(null);
    }

}
