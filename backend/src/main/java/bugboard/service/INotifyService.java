package bugboard.service;

import bugboard.dto.Notify;

import java.util.List;

/*
 * ISP + DIP — contratto delle notifiche. I controller dipendono da questa
 * astrazione, non dalla classe concreta. Il prefisso "I" è coerente con le
 * altre interfacce del progetto (IAuthService, IUserService, ...).
 */
public interface INotifyService {
    List<Notify> getNotificaPerUtente(Integer utenteId);
    boolean segnaComeLetta(int id);
    int contaNotificheNonLette(Integer utenteId);
    Integer getIssueDaNotifica(int id);
}
