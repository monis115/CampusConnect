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

@RestController
@RequestMapping("/api/admins")

public class AdminController {
	
	
//	For Admins   starts 

    @Autowired
    private AdminService adminService;

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
        	    "active".equalsIgnoreCase(existingAdmin.getStatus())  && "admin".equalsIgnoreCase(existingAdmin.getRole())) {
            return existingAdmin;
        }
        return null;
    }
    
    
    
//     For Admins ends
//     Admin -> agents starts here 
//  For Admins ends
//  Admin -> agents starts here 

@Autowired
private AgentService agentService;

//----------------------- AGENT MANAGEMENT -----------------------

//Fetch all agents
@GetMapping("/agents")
public List<Agent> getAllAgents() {
 return agentService.getAllAgents();
}

//Fetch agent by ID
@GetMapping("/agents/{id}")
public Optional<Agent> getAgentById(@PathVariable Long id) {
 return agentService.getAgentById(id);
}

//Register a new agent
@PostMapping("/agents/register")
public Agent registerAgent(@RequestBody AgentRegistrationDTO agent) {
 return agentService.registerAgent(agent);
}

//Update existing agent
@PutMapping("/agents/{id}")
public Agent updateAgent(@PathVariable Long id, @RequestBody AgentRegistrationDTO agent) {
 return agentService.updateAgent(id, agent);
}

//Delete agent
@DeleteMapping("/agents/{id}")
public String deleteAgent(@PathVariable Long id) {
 agentService.deleteAgent(id);
 return "Agent deleted successfully with ID: " + id;
}

//Agent login (optional)
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
