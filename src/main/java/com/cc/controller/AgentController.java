package com.cc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cc.DTO.AgentRegistrationDTO;
import com.cc.modals.Agent;
import com.cc.service.AgentService;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin("*")
public class AgentController {

    @Autowired
    private AgentService agentService;

    // ======================= SELF REGISTRATION =======================
    @PostMapping("/register")
    public Agent registerAgent(@RequestBody AgentRegistrationDTO agent) {
    	System.out.println(agent);
        return agentService.selfAgentRegistration(agent);
    }

    // ======================= ADMIN REGISTRATION =======================
    @PostMapping("/admin/register")
    public Agent registerAgentByAdmin(@RequestBody AgentRegistrationDTO agent) {
        return agentService.registerAgent(agent);
    }

    // ======================= GET ALL AGENTS =======================
    @GetMapping
    public List<Agent> getAllAgents() {
        return agentService.getAllAgents();
    }

    // ======================= GET PENDING AGENTS =======================
    @GetMapping("/pending")
    public List<Agent> getPendingAgents() {
        return agentService.getPendingAgents();
    }

    // ======================= GET AGENT BY ID =======================
    @GetMapping("/{id}")
    public Optional<Agent> getAgentById(@PathVariable Long id) {
        return agentService.getAgentById(id);
    }

    // ======================= GET AGENT BY USER ID =======================
    @GetMapping("/user/{userId}")
    public Optional<Agent> getAgentByUserId(@PathVariable String userId) {
        return agentService.getAgentByUserId(userId);
    }

    // ======================= APPROVE PENDING AGENT =======================
    @PostMapping("/approve/{agentId}")
    public Agent approveAgent(@PathVariable Long agentId, @RequestBody AgentRegistrationDTO dto) {
        return agentService.approveAgent(agentId, dto.getCollegeIds(), dto.getLocationIds());
    }

    // ======================= UPDATE AGENT =======================
    @PutMapping("/{id}")
    public Agent updateAgent(@PathVariable Long id, @RequestBody AgentRegistrationDTO dto) {
        return agentService.updateAgent(id, dto);
    }

    // ======================= DELETE AGENT =======================
    @DeleteMapping("/{id}")
    public String deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return "Agent deleted successfully with ID: " + id;
    }
    
    @GetMapping("/active")
    public List<Agent> getActiveAgents() {
        return agentService.getActiveAgents();
    }
    

    
}
