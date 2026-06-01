package bugboard.service;

import bugboard.dto.Notify;

import java.util.List;

/*
 * ISP + LSP — l'interfaccia dichiara l'intero contratto che le implementazioni
 * devono rispettare. In precedenza era troppo sottile (solo setNotificationRead),
 * costringendo tutti i controller a iniettare NotifyUIService (classe concreta)
 * per accedere ai metodi mancanti — violando sia ISP che DIP.
 */
public interface NotifyService {
    List<Notify> getNotificaPerUtente(Integer utenteId);
    boolean segnaComeLetta(int id);
    int contaNotificheNonLette(Integer utenteId);
    Integer getIssueDaNotifica(int id);
}
