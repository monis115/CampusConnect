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

    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with ID: " + id));
    }

    public Session updateSession(Long id, Session sessionDetails) {
        Session session = getSessionById(id);

        session.setSessionName(sessionDetails.getSessionName());
        session.setAcademicYear(sessionDetails.getAcademicYear());

        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session not found with ID: " + id);
        }
        sessionRepository.deleteById(id);
    }
}
