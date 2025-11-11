package com.cc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORT THIS

import com.cc.modals.AddmissionManager;
import com.cc.modals.College;
import com.cc.repository.AddmissionManagerRepository;
import com.cc.repository.CollegeRepository;

@Service
public class AddmissionManagerService {

    @Autowired
    private AddmissionManagerRepository addmissionManagerRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    private final Random random = new Random();


    @Transactional // <-- Add this
    public AddmissionManager createAddmissionManager(AddmissionManager manager, List<Long> collegeIds) {
        // fetch and assign colleges
        List<College> assignedColleges = new ArrayList<>();

        if (collegeIds != null) {
            for (Long collegeId : collegeIds) {
                collegeRepository.findById(collegeId).ifPresent(assignedColleges::add);
            }
        }

        manager.setColleges(assignedColleges);

        // set back-reference
        assignedColleges.forEach(c -> c.setAdmissionManager(manager));

        // generate username and ensure uniqueness
        String username = generateUniqueUsername(manager.getContactNumber());
        manager.setUsername(username);

        // password is the phone number per requirement, but store hashed
        String plainPassword = manager.getContactNumber();
        manager.setPassword(plainPassword); // You should hash this in a real app

        // persist
        return addmissionManagerRepository.save(manager);
    }

    // Utility: generate unique username according to rule
    private String generateUniqueUsername(String contactNumber) {
        String digits = contactNumber == null ? "" : contactNumber.replaceAll("\\D", "");
        String last3 = "000";
        if (digits.length() >= 3) {
            last3 = digits.substring(digits.length() - 3);
        } else {
            last3 = String.format("%3s", digits).replace(' ', '0');
        }
        String reversedLast3 = new StringBuilder(last3).reverse().toString();
        int year = LocalDate.now().getYear();
        String lastTwoYear = String.valueOf(year).substring(2);

        String candidate;
        int attempts = 0;
        do {
            String twoRandomDigits = String.format("%02d", random.nextInt(100));
            candidate = "M" + lastTwoYear + reversedLast3 + twoRandomDigits;
            attempts++;
            if (attempts > 1000) {
                candidate = "M" + lastTwoYear + reversedLast3 + System.currentTimeMillis() % 10000;
                break;
            }
        } while (addmissionManagerRepository.findByUsername(candidate).isPresent());

        return candidate;
    }

    public List<AddmissionManager> getAllManagers() {
        return addmissionManagerRepository.findAll();
    }

    public AddmissionManager getManagerById(Long id) {
        return addmissionManagerRepository.findById(id).orElse(null);
    }

  /**
   * This is the CORRECTED update method.
   */
  @Transactional // <-- Add this. It's critical.
  public AddmissionManager updateManager(Long id, AddmissionManager updatedManager, List<Long> collegeIds) {
    return addmissionManagerRepository.findById(id).map(existing -> {
        // Update scalar fields
        existing.setFullName(updatedManager.getFullName());
        existing.setEmail(updatedManager.getEmail());
        existing.setContactNumber(updatedManager.getContactNumber());
        existing.setStatus(updatedManager.getStatus());
        existing.setRole(updatedManager.getRole());

        // --- START OF CORRECT COLLEGE UPDATE LOGIC ---

        // 1. Unlink all old colleges.
        List<College> oldColleges = new ArrayList<>(existing.getColleges());
        for (College oldCollege : oldColleges) {
            oldCollege.setAdmissionManager(null); // Unlink the "many" side
        }
        
        // 2. Clear the managed collection. This will trigger orphan removal.
        existing.getColleges().clear();

        // 3. Fetch and add new colleges *directly* to the managed collection.
        if (collegeIds != null) { 
            for (Long collegeId : collegeIds) {
                // This checks if the college exists BEFORE trying to add it
                collegeRepository.findById(collegeId).ifPresent(newCollege -> {
                    newCollege.setAdmissionManager(existing);  // Link the "many" side
                    existing.getColleges().add(newCollege);  // <-- ADD TO THE MANAGED LIST
                });
            }
        }
        
        // 4. DO NOT call existing.setColleges()!
        
        // --- END OF CORRECT LOGIC ---

        // 5. Save the manager. Cascade.ALL will persist the changes.
        return addmissionManagerRepository.save(existing);
    }).orElse(null);
}

    @Transactional // <-- Add this
    public void deleteManager(Long id) {
        addmissionManagerRepository.findById(id).ifPresent(manager -> {
            manager.getColleges().forEach(college -> college.setAdmissionManager(null));
            manager.getColleges().clear();
            addmissionManagerRepository.save(manager);
        });

        addmissionManagerRepository.deleteById(id);
    }
    
    
    public List<AddmissionManager> getManagersWithStudents() {
        return addmissionManagerRepository.findByStudentsIsNotEmpty();
    }
    
    public Optional<AddmissionManager> getAdmissionManagerByUserName(String  userName) {
    	
    	return addmissionManagerRepository.findByUsername(userName);
    	
    }
}

