package com.cc.scheduler;

import com.cc.modals.TelexMan;
import com.cc.repository.TelexManRepository;
import com.cc.service.TelexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TelexReportScheduler {

    private final TelexManRepository telexManRepository;
    private final TelexService telexService;

    @Autowired
    public TelexReportScheduler(TelexManRepository telexManRepository, TelexService telexService) {
        this.telexManRepository = telexManRepository;
        this.telexService = telexService;
    }

    // Run daily at 11:55 PM
    @Scheduled(cron = "0 55 23 * * ?")
    public void sendDailyTelexReports() {
        System.out.println("Starting Daily Telex Report Generation...");

        List<TelexMan> allManagers = telexManRepository.findAll();
        LocalDate today = LocalDate.now();

        for (TelexMan manager : allManagers) {
            // Check if active (case-insensitive check for safety)
            if ("Active".equalsIgnoreCase(manager.getStatus())) {
                try {
                    System.out.println("Sending report to: " + manager.getUsername());
                    telexService.sendTelexReportEmail(manager.getUsername(), today, manager.getEmail(), "Daily");
                } catch (Exception e) {
                    System.err.println("Failed to send report to " + manager.getUsername() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Daily Telex Report Generation Completed.");
    }
}
