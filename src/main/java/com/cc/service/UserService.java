package com.cc.service;

import com.cc.modals.User;
import com.cc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Create new user
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // --- Resolve full name from username across all role repos ---
    @Autowired
    private com.cc.repository.AddmissionManagerRepository admissionManagerRepository;
    @Autowired
    private com.cc.repository.AgentRepository agentRepository;
    @Autowired
    private com.cc.repository.TelexManRepository telexManRepository;
    @Autowired
    private com.cc.repository.VisaManRepository visaManRepository;
    @Autowired
    private com.cc.repository.UniversityPersonRepository universityPersonRepository;
    @Autowired
    private com.cc.repository.DocPersonRepository docPersonRepository;
    @Autowired
    private com.cc.repository.AccountantRepository accountantRepository;

    public String resolveFullName(String username) {
        if (username == null || username.isEmpty() || username.equals("SYSTEM")) return username;

        // Check Admission Manager
        var manager = admissionManagerRepository.findByUsername(username);
        if (manager.isPresent()) return manager.get().getFullName() + " (Admission Manager)";

        // Check Agent
        var agent = agentRepository.findByUserId(username);
        if (agent.isPresent()) return agent.get().getFullName() + " (Agent)";

        // Check Telex Manager
        var telexMan = telexManRepository.findByUsername(username);
        if (telexMan.isPresent()) return telexMan.get().getFullName() + " (Telex Manager)";

        // Check Visa Manager
        var visaMan = visaManRepository.findByUsername(username);
        if (visaMan.isPresent()) return visaMan.get().getFullName() + " (Visa Manager)";

        // Check University Person
        var uniPerson = universityPersonRepository.findByUserName(username);
        if (uniPerson.isPresent()) return uniPerson.get().getFullName() + " (University Coordinator)";

        // Check Doc Person
        var docPerson = docPersonRepository.findByUsername(username);
        if (docPerson.isPresent()) return docPerson.get().getFullName() + " (Doc Person)";

        // Check Accountant
        var accountant = accountantRepository.findByUsername(username);
        if (accountant.isPresent()) return accountant.get().getFullName() + " (Accountant)";

        return username; // fallback to username if not found
    }
}
