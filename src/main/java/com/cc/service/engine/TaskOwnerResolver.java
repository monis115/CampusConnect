package com.cc.service.engine;

import com.cc.modals.Student;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Resolves the EXACT username of the person who should own a task
 * based on the Student's assigned staff relationships.
 */
@Service
public class TaskOwnerResolver {

    /**
     * Given a student and a role, returns the actual username of the assigned person.
     * Returns null if no person is assigned for that role.
     */
    public String resolveOwner(Student student, String role) {
        if (student == null || role == null) return null;

        switch (role.toUpperCase()) {
            case "ADMISSION_MANAGER":
                return student.getAdmissionManager() != null
                        ? student.getAdmissionManager().getUsername() : null;
            case "VISA_MANAGER":
                return student.getVisaMan() != null
                        ? student.getVisaMan().getUsername() : null;
            case "TELEX_MANAGER":
                return student.getTelexMan() != null
                        ? student.getTelexMan().getUsername() : null;
            case "DOCUMENT_MANAGER":
                return student.getDocPerson() != null
                        ? student.getDocPerson().getUsername() : null;
            case "ACCOUNTANT":
                return student.getAccountant() != null
                        ? student.getAccountant().getUsername() : null;
            case "UNIVERSITY_COORDINATOR":
                return student.getUniversityPerson() != null
                        ? student.getUniversityPerson().getUserName() : null;
            case "AGENT":
                return student.getAgent() != null
                        ? student.getAgent().getUserId() : null;
            default:
                return null;
        }
    }

    /**
     * Resolves watcher usernames from a comma-separated list of roles.
     * e.g., "ADMISSION_MANAGER,AGENT" → {"john_am", "agent_001"}
     */
    public Set<String> resolveWatchers(Student student, String watcherRoles) {
        Set<String> watchers = new HashSet<>();
        if (watcherRoles == null || watcherRoles.isEmpty()) return watchers;

        String[] roles = watcherRoles.split(",");
        for (String role : roles) {
            String username = resolveOwner(student, role.trim());
            if (username != null) {
                watchers.add(username);
            }
        }
        return watchers;
    }
}
