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
}
