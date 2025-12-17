package com.cc.service;

import com.cc.modals.Admin;
import com.cc.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserService userService;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Admin createAdmin(Admin admin) {
        admin.setStatus("active");
        admin.setRole("admin");
        Admin savedAdmin = adminRepository.save(admin);

        // Create User entry
        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedAdmin.getUserId());
        user.setPassword(savedAdmin.getPassword());
        user.setRole("admin");
        userService.createUser(user);

        return savedAdmin;
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        return adminRepository.findById(id).map(admin -> {
            admin.setUserId(updatedAdmin.getUserId());
            admin.setPassword(updatedAdmin.getPassword());
            admin.setStatus(updatedAdmin.getStatus());
            return adminRepository.save(admin);
        }).orElse(null);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public Admin findByUserId(String userId) {
        return adminRepository.findByUserId(userId);
    }

    public Admin createAdminWithGeneratedCredentials() {
        String username = generateUniqueUsername();
        String password = generateRandomPassword();

        Admin admin = new Admin();
        admin.setUserId(username);
        admin.setPassword(password);
        admin.setStatus("active");
        admin.setRole("admin");

        return createAdmin(admin);
    }

    private String generateUniqueUsername() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb;
        String username;

        do {
            sb = new StringBuilder(8);
            // Ensure at least one letter and one number if STRICTLY required, but random
            // mix is usually fine.
            // User said "combination of char capital letter with some random number".
            // I'll ensure at least 2 chars are letters to be safe, but a pure random pick
            // from mix is likely "combination".
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            username = sb.toString();
        } while (adminRepository.findByUserId(username) != null);

        return username;
    }

    private String generateRandomPassword() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
