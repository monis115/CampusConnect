package com.cc.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cc.modals.VisaMan;
import com.cc.repository.VisaManRepository;
import com.cc.service.VisaService;

@Component
public class VisaReportScheduler {

    @Autowired
    private VisaService visaService;

    @Autowired
    private VisaManRepository visaManRepository;

    // Run at 11:55 PM every day
    @Scheduled(cron = "0 55 23 * * *")
    public void sendDailyVisaReports() {
        System.out.println("Starting Daily Visa Manager Reports...");

        List<VisaMan> visaManagers = visaManRepository.findAll();

        for (VisaMan vm : visaManagers) {
            if ("Active".equalsIgnoreCase(vm.getStatus())) {
                try {
                    // Send report to the manager themselves
                    visaService.sendReportEmail("DAILY", vm.getUsername(), vm.getEmail());
                    System.out.println("Sent daily report to VisaManager: " + vm.getUsername());
                } catch (Exception e) {
                    System.err.println("Failed to send report to " + vm.getUsername() + ": " + e.getMessage());
                }
            }
        }

        // Optionally send a global report to Admins or a configured email
        // For now, let's assume we might need a separate configuration for Admin email,
        // but the prompt says "schedule email generation repot for the all and each
        // visaman"

        // Let's also trigger a global report if needed, but for now specific managers
        // is the requirement.
        System.out.println("Daily Visa Manager Reports completed.");
    }
}
