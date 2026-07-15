package com.cc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.DTO.NotificationDTO;
import com.cc.modals.Notification;
import com.cc.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationDTO createNotification(NotificationDTO dto) {
        Notification notification = new Notification(
                dto.getHeading(),
                dto.getContent(),
                dto.getType(),
                dto.getSenderUsername(),
                dto.getTargetUsername()
        );

        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    public List<NotificationDTO> getNotificationsForUser(String targetUsername) {
        List<Notification> notifications = notificationRepository.findByTargetUsernameOrderByCreatedAtDesc(targetUsername);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteAllNotificationsForUser(String targetUsername) {
        notificationRepository.deleteByTargetUsername(targetUsername);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getHeading(),
                notification.getContent(),
                notification.getType(),
                notification.getSenderUsername(),
                notification.getTargetUsername(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
