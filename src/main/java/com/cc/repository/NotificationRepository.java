package com.cc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTargetUsernameOrderByCreatedAtDesc(String targetUsername);

    List<Notification> findBySenderUsernameOrderByCreatedAtDesc(String senderUsername);
}
