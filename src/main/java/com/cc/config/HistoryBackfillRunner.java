package com.cc.config;

import com.cc.modals.StudentStatusHistory;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Component
public class HistoryBackfillRunner implements CommandLineRunner {

    @Autowired
    private StudentStatusHistoryRepository historyRepo;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("LOG: Starting one-time backfill of updatedByName with Roles...");
        List<StudentStatusHistory> allHistory = historyRepo.findAll();
        
        int updatedCount = 0;
        for (StudentStatusHistory history : allHistory) {
            String updatedByUsername = history.getUpdatedBy();
            if (updatedByUsername != null && !updatedByUsername.isEmpty() && !updatedByUsername.equals("SYSTEM")) {
                String newFullNameAndRole = userService.resolveFullName(updatedByUsername);
                
                // Only update if it actually resolved to something different or we want to force append the role
                history.setUpdatedByName(newFullNameAndRole);
                updatedCount++;
            } else if ("SYSTEM".equals(updatedByUsername)) {
                history.setUpdatedByName("System Auto-Action");
                updatedCount++;
            }
        }
        
        historyRepo.saveAll(allHistory);
        System.out.println("LOG: Backfill complete! Updated " + updatedCount + " history records.");
    }
}
