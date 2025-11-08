package com.cc.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.DTO.AgentRegistrationDTO;
import com.cc.modals.Agent;
import com.cc.modals.College;
import com.cc.modals.Location;
import com.cc.repository.AgentRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.LocationRepository;

@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private LocationRepository locationRepository;

    // ======================= ADMIN REGISTRATION (APPROVED) =======================
    @Transactional
    public Agent registerAgent(AgentRegistrationDTO agentDTO) {
        logger.info("Admin registering agent with email: {}", agentDTO.getEmail());

        validateEmailUniqueness(agentDTO.getEmail());

        Agent newAgent = new Agent();

        // === Personal Details ===
        newAgent.setFullName(agentDTO.getFullName());
        newAgent.setGender(agentDTO.getGender());
        newAgent.setAge(agentDTO.getAge());
        newAgent.setDob(agentDTO.getDob());
        newAgent.setEmail(agentDTO.getEmail());
        newAgent.setContactNo(agentDTO.getContactNo());
        newAgent.setAddress(agentDTO.getAddress());
        newAgent.setIdentityProofType(agentDTO.getIdentityProofType());
        newAgent.setIdentityProofNumber(agentDTO.getIdentityProofNumber());
        newAgent.setDesignation(agentDTO.getDesignation());
        newAgent.setExperience(agentDTO.getExperience());

        // === Company Details ===
        newAgent.setCompanyName(agentDTO.getCompanyName());
        newAgent.setRegisteredBusinessName(agentDTO.getRegisteredBusinessName());
        newAgent.setBusinessType(agentDTO.getBusinessType());
        newAgent.setRegistrationNumber(agentDTO.getRegistrationNumber());
        newAgent.setPanOrGstNumber(agentDTO.getPanOrGstNumber());
        newAgent.setRegisteredOfficeAddress(agentDTO.getRegisteredOfficeAddress());
        newAgent.setBranchOfficeAddress(agentDTO.getBranchOfficeAddress());
        newAgent.setCompanyEmail(agentDTO.getCompanyEmail());
        newAgent.setCompanyContactNo(agentDTO.getCompanyContactNo());
        newAgent.setWebsite(agentDTO.getWebsite());

        // === Dates & Status ===
        newAgent.setCreationDate(LocalDate.now());
        newAgent.setActivationDate(LocalDate.now());
        newAgent.setStatus("Active");

        // === College Privileges ===
        List<College> colleges = fetchCollegesByIds(agentDTO.getCollegeIds());
        newAgent.setCollegePrivileges(colleges);

        // === Location Privileges ===
        List<Location> locations = fetchLocationsByIds(agentDTO.getLocationIds());
        newAgent.setAccessibleLocations(locations);

        // === Login Credentials ===
        String userId = generateUniqueUserId(agentDTO.getAge(), agentDTO.getDob().getYear());
        String password = agentDTO.getDob().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        newAgent.setUserId(userId);
        newAgent.setPassword(password); // TODO: Hash in production!

        Agent savedAgent = agentRepository.save(newAgent);
        logger.info("Agent registered successfully with ID: {}", savedAgent.getId());
        return savedAgent;
    }

    // ======================= SELF REGISTRATION (PENDING APPROVAL) =======================
    @Transactional
    public Agent selfAgentRegistration(AgentRegistrationDTO agentDTO) {
        logger.info("Self-registration request for email: {}", agentDTO.getEmail());

        validateEmailUniqueness(agentDTO.getEmail());

        Agent newAgent = new Agent();

        // === Personal Details ===
        newAgent.setFullName(agentDTO.getFullName());
        newAgent.setGender(agentDTO.getGender());
        newAgent.setAge(agentDTO.getAge());
        newAgent.setDob(agentDTO.getDob());
        newAgent.setEmail(agentDTO.getEmail());
        newAgent.setContactNo(agentDTO.getContactNo());
        newAgent.setAddress(agentDTO.getAddress());
        newAgent.setIdentityProofType(agentDTO.getIdentityProofType());
        newAgent.setIdentityProofNumber(agentDTO.getIdentityProofNumber());
        newAgent.setDesignation(agentDTO.getDesignation());
        newAgent.setExperience(agentDTO.getExperience());

        // === Company Details ===
        newAgent.setCompanyName(agentDTO.getCompanyName());
        newAgent.setRegisteredBusinessName(agentDTO.getRegisteredBusinessName());
        newAgent.setBusinessType(agentDTO.getBusinessType());
        newAgent.setRegistrationNumber(agentDTO.getRegistrationNumber());
        newAgent.setPanOrGstNumber(agentDTO.getPanOrGstNumber());
        newAgent.setRegisteredOfficeAddress(agentDTO.getRegisteredOfficeAddress());
        newAgent.setBranchOfficeAddress(agentDTO.getBranchOfficeAddress());
        newAgent.setCompanyEmail(agentDTO.getCompanyEmail());
        newAgent.setCompanyContactNo(agentDTO.getCompanyContactNo());
        newAgent.setWebsite(agentDTO.getWebsite());

        // === Dates & Status ===
        newAgent.setCreationDate(LocalDate.now());
        newAgent.setActivationDate(null); // Will be set on approval
        newAgent.setStatus("pending");

        // === No privileges until approved ===
        newAgent.setCollegePrivileges(new ArrayList<>());
        newAgent.setAccessibleLocations(new ArrayList<>());

        // === Login Credentials ===
        String userId = generateUniqueUserId(agentDTO.getAge(), agentDTO.getDob().getYear());
        String password = agentDTO.getDob().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        newAgent.setUserId(userId);
        newAgent.setPassword(password);

        Agent savedAgent = agentRepository.save(newAgent);
        logger.info("Self-registered agent saved with ID: {} | Status: Pending", savedAgent.getId());
        return savedAgent;
    }

    // ======================= APPROVE PENDING AGENT =======================
    @Transactional
    public Agent approveAgent(Long agentId, List<Long> collegeIds, List<Long> locationIds) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));

        if (!"Pending Approval".equals(agent.getStatus())) {
            throw new RuntimeException("Agent is not in pending state");
        }

        agent.setStatus("Active");
        agent.setActivationDate(LocalDate.now());

        List<College> colleges = fetchCollegesByIds(collegeIds);
        List<Location> locations = fetchLocationsByIds(locationIds);

        agent.setCollegePrivileges(colleges);
        agent.setAccessibleLocations(locations);

        return agentRepository.save(agent);
    }

    // ======================= HELPER METHODS =======================
    private void validateEmailUniqueness(String email) {
        if (agentRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }
    }

    private List<College> fetchCollegesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<College> colleges = new ArrayList<>();
        for (Long id : ids) {
            College college = collegeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("College not found with ID: " + id));
            colleges.add(college);
        }
        return colleges;
    }

    private List<Location> fetchLocationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        for (Long id : ids) {
            Location location = locationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found with ID: " + id));
            locations.add(location);
        }
        return locations;
    }

    private String generateUniqueUserId(int age, int birthYear) {
        Random random = new Random();
        String userId;
        do {
            String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMyy"));
            int randomDigits = 10 + random.nextInt(90); // 10–99
            String yearPart = String.valueOf(birthYear).substring(2); // last 2 digits
            userId = "R" + monthYear + age + yearPart + randomDigits;
        } while (agentRepository.existsByUserId(userId));
        return userId;
    }

    // ======================= CRUD METHODS =======================
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<Agent> getPendingAgents() {
        return agentRepository.findByStatus("pending");
    }

    public Optional<Agent> getAgentById(Long id) {
        return agentRepository.findById(id);
    }

    public Optional<Agent> getAgentByUserId(String userId) {
        return agentRepository.findByUserId(userId);
    }

    @Transactional
    public Agent updateAgent(Long id, AgentRegistrationDTO dto) {
        return agentRepository.findById(id)
                .map(agent -> {
                    // Update personal
                    agent.setFullName(dto.getFullName());
                    agent.setGender(dto.getGender());
                    agent.setAge(dto.getAge());
                    agent.setDob(dto.getDob());
                    agent.setEmail(dto.getEmail());
                    agent.setContactNo(dto.getContactNo());
                    agent.setAddress(dto.getAddress());
                    agent.setIdentityProofType(dto.getIdentityProofType());
                    agent.setIdentityProofNumber(dto.getIdentityProofNumber());
                    agent.setDesignation(dto.getDesignation());
                    agent.setExperience(dto.getExperience());

                    // Update company
                    agent.setCompanyName(dto.getCompanyName());
                    agent.setRegisteredBusinessName(dto.getRegisteredBusinessName());
                    agent.setBusinessType(dto.getBusinessType());
                    agent.setRegistrationNumber(dto.getRegistrationNumber());
                    agent.setPanOrGstNumber(dto.getPanOrGstNumber());
                    agent.setRegisteredOfficeAddress(dto.getRegisteredOfficeAddress());
                    agent.setBranchOfficeAddress(dto.getBranchOfficeAddress());
                    agent.setCompanyEmail(dto.getCompanyEmail());
                    agent.setCompanyContactNo(dto.getCompanyContactNo());
                    agent.setWebsite(dto.getWebsite());
                    agent.setStatus("active");

                    // Update privileges
                    agent.setCollegePrivileges(fetchCollegesByIds(dto.getCollegeIds()));
                    agent.setAccessibleLocations(fetchLocationsByIds(dto.getLocationIds()));

                    return agentRepository.save(agent);
                })
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + id));
    }

    @Transactional
    public void deleteAgent(Long id) {
        if (!agentRepository.existsById(id)) {
            throw new RuntimeException("Agent not found with ID: " + id);
        }
        agentRepository.deleteById(id);
    }
    
    public List<Agent> getActiveAgents() {
        return agentRepository.findByStatus("Active");
    }
    
    public List<Agent> getAgentsWithStudents() {
        return agentRepository.findAgentsWithStudents();
    }
}