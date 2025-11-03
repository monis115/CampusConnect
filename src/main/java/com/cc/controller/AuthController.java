package com.cc.controller;

import com.cc.modals.User;
import com.cc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    // -------------------------
    // Authenticate User (Login)
    // -------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        Optional<User> userOpt = userService.getUserByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getPassword().equals(password)) {
                // ✅ Return full user details (excluding password if needed)
                return ResponseEntity.ok(user);
            } else {
                // ❌ Invalid password
                return ResponseEntity
                        .status(401)
                        .body("Invalid credentials. Please check your password.");
            }
        } else {
            // ❌ Username not found
            return ResponseEntity
                    .status(404)
                    .body("User not found. Please check your username.");
        }
    }
    
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
