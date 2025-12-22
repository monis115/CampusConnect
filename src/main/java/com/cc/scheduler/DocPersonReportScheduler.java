package com.cc.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cc.modals.DocPerson;
import com.cc.repository.DocPersonRepository;
import com.cc.service.DocPersonService;

@Component
public class DocPersonReportScheduler {

    @Autowired
    private DocPersonService docPersonService;

    @Autowired
    private DocPersonRepository docPersonRepository;

    // Run at 11:55 PM every day
    @Scheduled(cron = "0 55 23 * * *")
    public void sendDailyDocPersonReports() {
        System.out.println("Starting Daily Doc Manager Reports...");

        List<DocPerson> docPersons = docPersonRepository.findAll();

        for (DocPerson dp : docPersons) {
            if ("Active".equalsIgnoreCase(dp.getStatus())) {
                try {
                    // Send report to the manager themselves
                    docPersonService.sendReportEmail("Today", dp.getUsername(), dp.getEmail());
                    System.out.println("Sent daily report to DocPerson: " + dp.getUsername());
                } catch (Exception e) {
                    System.err.println("Failed to send report to " + dp.getUsername() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Daily Doc Manager Reports completed.");
    }
}
