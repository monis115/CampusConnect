package com.cc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.modals.Session;
import com.cc.repository.SessionRepository;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public List<Session> getActiveSessions() {
        return sessionRepository.findByStatus("ACTIVE");
    }

    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with ID: " + id));
    }

    public Session updateSession(Long id, Session sessionDetails) {
        Session session = getSessionById(id);

        if (sessionDetails.getSessionName() != null) {
            session.setSessionName(sessionDetails.getSessionName());
        }
        if (sessionDetails.getAcademicYear() != null) {
            session.setAcademicYear(sessionDetails.getAcademicYear());
        }
        if (sessionDetails.getStatus() != null) {
            session.setStatus(sessionDetails.getStatus());
        }

        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session not found with ID: " + id);
        }
        sessionRepository.deleteById(id);
    }
}
