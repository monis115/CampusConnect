package com.cc.scheduler;

import com.cc.service.AddmissionManagerService;
import com.cc.service.UniversityPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that automatically sends daily performance reports via email.
 * Runs every night at 11:55 PM.
 */
@Component
public class DailyReportScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyReportScheduler.class);

    @Autowired
    private AddmissionManagerService admissionManagerService;

    @Autowired
    private UniversityPersonService universityPersonService;

    // Configure the recipient email in application.properties
    // Example: report.email.recipient=admin@campusconnect.com
    @Value("${report.email.recipient:admin@example.com}")
    private String reportRecipientEmail;

    // Enable/disable the scheduler via config
    @Value("${report.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * Sends daily performance reports automatically.
     * Cron expression: second minute hour day-of-month month day-of-week
     * "0 55 23 * * ?" = At 23:55:00 (11:55 PM) every day - PRODUCTION
     */
    @Scheduled(cron = "0 55 23 * * ?")
    public void sendDailyReport() {
        if (!schedulerEnabled) {
            logger.info("Daily report scheduler is disabled. Skipping.");
            return;
        }

        logger.info("⏰ Starting scheduled daily reports at 11:55 PM...");

        // Send Admission Manager reports
        try {
            admissionManagerService.sendAllManagersReportByEmail("daily", reportRecipientEmail);
            logger.info("✅ Admission Manager daily report sent to: {}", reportRecipientEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send Admission Manager report: {}", e.getMessage(), e);
        }

        // Send University Coordinator reports
        try {
            universityPersonService.sendAllReportsEmail("daily", reportRecipientEmail);
            logger.info("✅ University Coordinator daily report sent to: {}", reportRecipientEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send University Coordinator report: {}", e.getMessage(), e);
        }
    }

    /**
     * Optional: Send weekly summary every Sunday at 11:55 PM
     * "0 55 23 ? * SUN" = At 23:55:00 every Sunday
     */
    @Scheduled(cron = "0 55 23 ? * SUN")
    public void sendWeeklySummary() {
        if (!schedulerEnabled) {
            return;
        }

        logger.info("📅 Starting weekly summary reports...");

        // Send Admission Manager weekly report
        try {
            admissionManagerService.sendAllManagersReportByEmail("monthly", reportRecipientEmail);
            logger.info("✅ Admission Manager weekly summary sent to: {}", reportRecipientEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send Admission Manager weekly summary: {}", e.getMessage(), e);
        }

        // Send University Coordinator weekly report
        try {
            universityPersonService.sendAllReportsEmail("monthly", reportRecipientEmail);
            logger.info("✅ University Coordinator weekly summary sent to: {}", reportRecipientEmail);
        } catch (Exception e) {
            logger.error("❌ Failed to send University Coordinator weekly summary: {}", e.getMessage(), e);
        }
    }
}
