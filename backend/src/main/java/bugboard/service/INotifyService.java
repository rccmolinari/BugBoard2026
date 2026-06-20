package bugboard.service;

import bugboard.dto.Notify;

import java.util.List;

/*
 * Il contratto per le notifiche: leggerle, segnarle come lette, contarle e
 * risalire alla issue collegata. Il controller si appoggia a questa.
 */
public interface INotifyService {
    List<Notify> getNotificaPerUtente(Integer utenteId);
    boolean segnaComeLetta(int id);
    int contaNotificheNonLette(Integer utenteId);
    Integer getIssueDaNotifica(int id);
}
