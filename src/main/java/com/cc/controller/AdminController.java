package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cc.DTO.AgentRegistrationDTO;
import com.cc.modals.Admin;
import com.cc.modals.Agent;
import com.cc.service.AdminService;
import com.cc.service.AgentService;
import com.cc.service.EmailService;
import com.cc.service.OTPService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admins")

public class AdminController {

    // For Admins starts

    @Autowired
    private AdminService adminService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @Value("${admin.contact.email}")
    private String adminContactEmail;

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/{id}")
    public Optional<Admin> getAdminById(@PathVariable Long id) {
        return adminService.getAdminById(id);
    }

    @PostMapping
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminService.createAdmin(admin);
    }

    @PutMapping("/{id}")
    public Admin updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        return adminService.updateAdmin(id, admin);
    }

    @DeleteMapping("/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return "Admin deleted successfully with ID: " + id;
    }

    // Optional login endpoint
    @PostMapping("/login")
    public Admin login(@RequestBody Admin loginRequest) {
        Admin existingAdmin = adminService.findByUserId(loginRequest.getUserId());
        if (existingAdmin != null && existingAdmin.getPassword().equals(loginRequest.getPassword()) &&
                "active".equalsIgnoreCase(existingAdmin.getStatus())
                && "admin".equalsIgnoreCase(existingAdmin.getRole())) {
            return existingAdmin;
        }
        return null;
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<String> sendVerificationOtp() {
        String otp = otpService.generateOtp(adminContactEmail);
        String subject = "Admin Creation Verification Code";
        String message = "Your verification code for creating a new Admin account is: " + otp
                + "\n\nThis code is valid for 5 minutes.";
        emailService.sendEmail(adminContactEmail, subject, message);
        return ResponseEntity.ok("OTP sent to the registered admin email.");
    }

    @PostMapping("/verify-and-create")
    public ResponseEntity<?> verifyAndCreateAdmin(@RequestParam String otp) {
        if (otpService.validateOtp(adminContactEmail, otp)) {
            Admin newAdmin = adminService.createAdminWithGeneratedCredentials();

            // Send Credentials Email
            String subject = "New Admin Account Credentials";
            String htmlContent = generateAdminCredentialsEmail(newAdmin.getUserId(), newAdmin.getPassword());
            emailService.sendHtmlEmail(adminContactEmail, subject, htmlContent);

            return ResponseEntity.ok(newAdmin);
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }

    private String generateAdminCredentialsEmail(String username, String password) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px; background-color: #f9f9f9;'>"
                +
                "<h2 style='color: #4CAF50; text-align: center;'>Welcome to CampusConnect</h2>" +
                "<p style='font-size: 16px; text-align: center;'>A new Administrator account has been successfully created.</p>"
                +
                "<div style='background-color: #fff; padding: 20px; border-radius: 8px; margin-top: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                +
                "<p style='font-size: 18px; margin: 0;'><strong>Username:</strong> <span style='color: #007BFF;'>"
                + username + "</span></p>" +
                "<p style='font-size: 18px; margin: 10px 0 0;'><strong>Password:</strong> <span style='color: #D32F2F;'>"
                + password + "</span></p>" +
                "</div>" +
                "<p style='text-align: center; margin-top: 30px; font-size: 14px; color: #777;'>Please change your password after your first login for security purposes.</p>"
                +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<p style='font-size: 12px; color: #aaa;'>&copy; 2025 CampusConnect. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // For Admins ends
    // Admin -> agents starts here
    // For Admins ends
    // Admin -> agents starts here

    @Autowired
    private AgentService agentService;

    // ----------------------- AGENT MANAGEMENT -----------------------

    // Fetch all agents
    @GetMapping("/agents")
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }

    // Fetch agent by ID
    @GetMapping("/agents/{id}")
    public Optional<Agent> getAgentById(@PathVariable Long id) {
        return agentService.getAgentById(id);
    }

    // Register a new agent
    @PostMapping("/agents/register")
    public Agent registerAgent(@RequestBody AgentRegistrationDTO agent) {
        return agentService.registerAgent(agent);
    }

    // Update existing agent
    @PutMapping("/agents/{id}")
    public Agent updateAgent(@PathVariable Long id, @RequestBody AgentRegistrationDTO agent) {
        return agentService.updateAgent(id, agent);
    }

    // Delete agent
    @DeleteMapping("/agents/{id}")
    public String deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return "Agent deleted successfully with ID: " + id;
    }

    // Agent login (optional)
    @PostMapping("/agents/login")
    public String agentLogin(@RequestBody Agent loginRequest) {
        Optional<Agent> agentOpt = agentService.getAllAgents().stream()
                .filter(a -> a.getUserId().equals(loginRequest.getUserId()))
                .findFirst();

        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            if (agent.getPassword().equals(loginRequest.getPassword()) &&
                    "active".equalsIgnoreCase(agent.getStatus())) {
                return "Agent login successful for UserID: " + agent.getUserId();
            }
        }
        return "Invalid credentials or inactive account";
    }

}
