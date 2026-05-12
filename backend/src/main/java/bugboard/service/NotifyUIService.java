package bugboard.service;

import org.springframework.stereotype.Service;

@Service
public class NotifyUIService implements NotifyService {
    @Override
    public void sendNotification(String message) {
        // Logica per inviare una notifica all'interfaccia utente
        
        System.out.println("Notifica UI: " + message);
    }
    @Override
    public void setNotificationRead(int notificationId) {
        // Logica per segnare una notifica come letta nell'interfaccia utente
        System.out.println("Notifica con ID " + notificationId + " segnata come letta.");
    }
    
}
