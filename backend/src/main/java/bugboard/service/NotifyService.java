package bugboard.service;

public interface NotifyService {
    void sendNotification(String message);
    void setNotificationRead(int notificationId);
}
