package bugboard.service;

public class NotifyUIService implements NotifyService {
    @Override
    public void sendNotification(String message) {
        // Logica per inviare una notifica all'interfaccia utente
        System.out.println("Notifica UI: " + message);
    }
    
}
