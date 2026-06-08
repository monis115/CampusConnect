package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a simple text email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param content Email content (plain text)
     */
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send an HTML email.
     *
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param htmlContent Email content (HTML)
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(mimeMessage);
            System.out.println("HTML Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

    /**
     * Send email to multiple recipients.
     *
     * @param toList  Array of recipient email addresses
     * @param subject Email subject
     * @param content Email content
     */
    public void sendEmailToMultiple(String[] toList, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toList);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("Email sent successfully to " + toList.length + " recipients");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send Collection Reminder Email.
     */
    public void sendCollectionReminder(String to, com.cc.modals.CollectionRequest req, String templateType) {
        String subject = "Collection Request Update: " + req.getRequestId();
        
        StringBuilder html = new StringBuilder();
        html.append("<h2>Collection Request Update (").append(templateType).append(")</h2>");
        html.append("<p><strong>Request ID:</strong> ").append(req.getRequestId()).append("</p>");
        html.append("<p><strong>Student:</strong> ").append(req.getStudentName()).append(" (").append(req.getApplicationId()).append(")</p>");
        html.append("<p><strong>Requested Amount:</strong> ").append(req.getRequestedAmount()).append("</p>");
        html.append("<p><strong>Collected Amount:</strong> ").append(req.getCollectedAmount()).append("</p>");
        html.append("<p><strong>Outstanding Amount:</strong> ").append(req.getPendingAmount()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(req.getStatus()).append("</p>");
        html.append("<p><strong>Due Date:</strong> ").append(req.getDueDate() != null ? req.getDueDate().toString() : "N/A").append("</p>");
        
        if (req.getRemarks() != null) {
            html.append("<p><strong>Remarks:</strong> ").append(req.getRemarks()).append("</p>");
        }
        
        html.append("<br/><p>Please process the pending amount at your earliest convenience.</p>");
        
        sendHtmlEmail(to, subject, html.toString());
    }
}
