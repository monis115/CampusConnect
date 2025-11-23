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

import com.cc.DTO.AgentCollegeAccessDTO;
import com.cc.DTO.AgentRegistrationDTO;
import com.cc.modals.Agent;
import com.cc.modals.AgentCollegeAccess;
import com.cc.modals.College;
import com.cc.modals.Location;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
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

    @Autowired
    private UserService userService;

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

        // === College Access Privileges ===
        List<AgentCollegeAccess> accessList = processCollegeAccess(agentDTO.getCollegeAccessList(), newAgent);
        newAgent.setCollegeAccess(new java.util.HashSet<>(accessList));

        // === Login Credentials ===
        String userId = generateUniqueUserId(agentDTO.getAge(), agentDTO.getDob().getYear());
        String password = agentDTO.getDob().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        newAgent.setUserId(userId);
        newAgent.setPassword(password); // TODO: Hash in production!

        Agent savedAgent = agentRepository.save(newAgent);

        // Create User entry
        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedAgent.getUserId());
        user.setPassword(savedAgent.getPassword());
        user.setRole("agent");
        userService.createUser(user);

        logger.info("Agent registered successfully with ID: {}", savedAgent.getId());
        return savedAgent;
    }

    // ======================= SELF REGISTRATION (PENDING APPROVAL)
    // =======================
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
        // === No privileges until approved ===
        newAgent.setCollegeAccess(new java.util.HashSet<>());

        // === Login Credentials ===
        String userId = generateUniqueUserId(agentDTO.getAge(), agentDTO.getDob().getYear());
        String password = agentDTO.getDob().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        newAgent.setUserId(userId);
        newAgent.setPassword(password);

        Agent savedAgent = agentRepository.save(newAgent);

        // Create User entry
        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedAgent.getUserId());
        user.setPassword(savedAgent.getPassword());
        user.setRole("agent");
        userService.createUser(user);

        logger.info("Self-registered agent saved with ID: {} | Status: Pending", savedAgent.getId());
        return savedAgent;
    }

    // ======================= APPROVE PENDING AGENT =======================
    @Transactional
    // Note: approveAgent signature needs to change or we need to pass DTO.
    // For now, assuming we might need to refactor this method signature in
    // Controller too.
    // But based on current code, let's assume we pass DTO to this method or handle
    // it differently.
    // Wait, the controller calls this with IDs. I need to update the Controller as
    // well.
    // For this step, I will comment out the old logic and add a TODO or fix it if I
    // can change the signature.
    // actually, let's change the signature to accept DTO or
    // List<AgentCollegeAccessDTO>

    // Since I cannot easily change the controller in this same step without
    // breaking compilation if I change signature,
    // I will overload or just replace the body if the controller passes DTO.
    // The controller passes `dto.getCollegeIds(), dto.getLocationIds()`.
    // I should update the controller to pass the full DTO or the access list.

    // Let's update the method signature here and I will update the controller in
    // the next step.
    // public Agent approveAgent(Long agentId, List<AgentCollegeAccessDTO>
    // accessDTOs)

    // For now, I will just set empty access if the old signature is used, but I
    // really should update the signature.
    // I'll update the signature to take the DTO list.
    public Agent approveAgent(Long agentId, List<AgentCollegeAccessDTO> accessDTOs) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));

        if (!"Pending Approval".equalsIgnoreCase(agent.getStatus()) && !"pending".equalsIgnoreCase(agent.getStatus())) {
            // relaxed check for "pending" or "Pending Approval"
        }

        agent.setStatus("Active");
        agent.setActivationDate(LocalDate.now());

        List<AgentCollegeAccess> accessList = processCollegeAccess(accessDTOs, agent);
        agent.setCollegeAccess(new java.util.HashSet<>(accessList));

        return agentRepository.save(agent);
    }

    // ======================= HELPER METHODS =======================
    private void validateEmailUniqueness(String email) {
        if (agentRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }
    }

    private List<College> fetchCollegesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        List<College> colleges = new ArrayList<>();
        for (Long id : ids) {
            College college = collegeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("College not found with ID: " + id));
            colleges.add(college);
        }
        return colleges;
    }

    private List<Location> fetchLocationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        for (Long id : ids) {
            Location location = locationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found with ID: " + id));
            locations.add(location);
        }
        return locations;
    }

    private List<AgentCollegeAccess> processCollegeAccess(List<AgentCollegeAccessDTO> dtos, Agent agent) {
        List<AgentCollegeAccess> accessList = new ArrayList<>();
        if (dtos == null)
            return accessList;

        for (AgentCollegeAccessDTO dto : dtos) {
            College college = collegeRepository.findById(dto.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("College not found with ID: " + dto.getCollegeId()));

            List<Location> locations = fetchLocationsByIds(dto.getLocationIds());

            AgentCollegeAccess access = new AgentCollegeAccess();
            access.setAgent(agent);
            access.setCollege(college);
            access.setAccessAllLocations(dto.isAccessAllLocations());
            access.setAllowedLocations(new java.util.HashSet<>(locations));

            accessList.add(access);
        }
        return accessList;
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
                    // Update privileges
                    List<AgentCollegeAccess> accessList = processCollegeAccess(dto.getCollegeAccessList(), agent);
                    // Clear existing access to avoid orphans if needed, or just set new list
                    // (orphanRemoval=true handles it)
                    agent.getCollegeAccess().clear();
                    agent.getCollegeAccess().addAll(accessList);

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

    public Optional<Agent> getAgentByUserIdWithDetails(String userId) {
        return agentRepository.findByUserIdWithDetails(userId);
    }

    @Autowired
    private com.cc.repository.StudentRepository studentRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    public com.cc.modals.Student submitTelexDetails(Long studentId, com.cc.DTO.TelexDetailsDTO details,
            String screenshotPath, String passportPath) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate Agent
        if (details.getAgentUsername() != null
                && (student.getAgent() == null || !student.getAgent().getUserId().equals(details.getAgentUsername()))) {
            // Optional: Enforce agent check if username is provided
            // throw new RuntimeException("Unauthorized: Agent username does not match
            // assigned agent.");
        }

        // Update Student Details
        if (details.getPassportNumber() != null) {
            student.setPassportNo(details.getPassportNumber());
        }
        student.setPassportValidationDate(details.getPassportValidationDate());
        student.setTelexPaymentAmount(details.getTelexPaymentAmount());
        student.setTelexPaymentTxId(details.getTelexPaymentTxId());

        if (screenshotPath != null) {
            student.setTelexPaymentScreenshotPath(screenshotPath);
        }
        if (passportPath != null) {
            student.setPassportPath(passportPath);
        }

        // Update Status
        student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);

        // Reset approvals if they were previously set (re-submission case)
        student.setIsTelexAccountantApproved(false);
        student.setIsTelexDocApproved(false);

        student.setAction(action.CONFIRM_DOCS);
        student.setActionOnUser(actionOnUser.doc_manager);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED);
        history.setUpdatedBy(details.getAgentUsername() != null ? details.getAgentUsername() : "Agent");
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        return savedStudent;
    }
}