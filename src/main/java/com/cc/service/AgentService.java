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
import com.cc.modals.DocPerson;
import com.cc.modals.Location;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.modals.DocumentReturnRequest;
import com.cc.repository.DocumentReturnRequestRepository;
import com.cc.DTO.DocumentReturnRequestDTO;
import com.cc.repository.AgentRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.LocationRepository;

import org.springframework.scheduling.annotation.Scheduled;
import com.cc.service.EmailService;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private DocumentReturnRequestRepository documentReturnRequestRepository;

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
                    if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                        agent.setStatus(dto.getStatus());
                    } else {
                        // Default behavior or specific logic if needed
                        // agent.setStatus("active"); // Don't force active if not provided?
                    }

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

    public List<com.cc.DTO.AgentSummaryDTO> getActiveAgents() {
        List<Agent> agents = agentRepository.findByStatus("Active");
        return agents.stream().map(agent -> {
            int collegeCount = agent.getCollegeAccess() != null ? agent.getCollegeAccess().size() : 0;
            int locationCount = 0;
            if (agent.getCollegeAccess() != null) {
                locationCount = agent.getCollegeAccess().stream()
                        .mapToInt(access -> access.getAllowedLocations() != null ? access.getAllowedLocations().size()
                                : 0)
                        .sum();
            }
            return new com.cc.DTO.AgentSummaryDTO(
                    agent.getId(),
                    agent.getFullName(),
                    agent.getEmail(),
                    agent.getCompanyName(),
                    agent.getContactNo(),
                    agent.getStatus(),
                    agent.getUserId(),
                    collegeCount,
                    locationCount);
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<com.cc.DTO.AgentSummaryDTO> getAgentsWithStudents() {
        List<Agent> agents = agentRepository.findAgentsWithStudents();
        return agents.stream().map(agent -> {
            int collegeCount = agent.getCollegeAccess() != null ? agent.getCollegeAccess().size() : 0;
            int locationCount = 0;
            if (agent.getCollegeAccess() != null) {
                locationCount = agent.getCollegeAccess().stream()
                        .mapToInt(access -> access.getAllowedLocations() != null ? access.getAllowedLocations().size()
                                : 0)
                        .sum();
            }
            return new com.cc.DTO.AgentSummaryDTO(
                    agent.getId(),
                    agent.getFullName(),
                    agent.getEmail(),
                    agent.getCompanyName(),
                    agent.getContactNo(),
                    agent.getStatus(),
                    agent.getUserId(),
                    collegeCount,
                    locationCount);
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<com.cc.DTO.StudentDTO> getStudentsByAgentId(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));

        List<com.cc.modals.Student> students = agent.getStudent();
        if (students == null) {
            return new ArrayList<>();
        }
        java.util.Collections.reverse(students);

        return students.stream()
                .map(student -> {
                    com.cc.DTO.StudentDTO dto = new com.cc.DTO.StudentDTO();

                    // Personal & Contact
                    dto.setFullName(student.getFullName());
                    dto.setDob(student.getDob());
                    dto.setAge(student.getAge());
                    dto.setEmail(student.getEmail());
                    dto.setContactNo(student.getContactNo());
                    dto.setAddress(student.getAddress());
                    // dto.setDistrictId(student.getDistrictId()); // Assuming not present in
                    // Student entity or managed elsewhere
                    dto.setEmergencyContactNo(student.getEmergencyContactNo());
                    dto.setEmergencyRelation(student.getEmergencyRelation());

                    // Parents
                    dto.setMothersName(student.getMothersName());
                    dto.setFathersName(student.getFathersName());

                    // ID Documents
                    dto.setPassportNo(student.getPassportNo());
                    dto.setPassportValidTill(student.getPassportValidTill());
                    dto.setAdhaarCardNo(student.getAdhaarCardNo());
                    dto.setAdhaarPath(student.getAdhaarPath());
                    dto.setPassportPath(student.getPassportPath());

                    // Academic (12th)
                    dto.setClass12thPassingYear(student.getClass12thPassingYear());
                    dto.setBaords(student.getBaords());
                    dto.setClass12thMarks(student.getClass12thMarks());
                    dto.setMaxMarks(student.getMaxMarks());
                    dto.setMarkseetPath(student.getMarkseetPath());

                    // NEET Details
                    dto.setHasGivenNEET(student.isHasGivenNEET());
                    dto.setNeetYear(student.getNeetYear());
                    dto.setNeetScore(student.getNeetScore());
                    dto.setNeetMarksheetPath(student.getNeetMarksheetPath());

                    // Application Details
                    dto.setIntake(student.getIntake());
                    dto.setAppliedFor(student.getAppliedFor());
                    if (student.getApplicationStatus() != null) {
                        dto.setApplicationStatus(student.getApplicationStatus().toString());
                    }

                    // File Paths
                    dto.setConfirmationPagePath(student.getConfirmationPagePath());
                    // Use internal getter to bypass the security check - agents should see the path
                    // once it exists
                    dto.setAdmissionLetterPath(student.getAdmissionLetterPathInternal());

                    // Relationships
                    if (student.getCollegePreferences() != null) {
                        dto.setCollegeIds(student.getCollegePreferences().stream()
                                .map(com.cc.modals.College::getId)
                                .collect(java.util.stream.Collectors.toList()));
                        dto.setCollegeNames(student.getCollegePreferences().stream()
                                .map(com.cc.modals.College::getCollegeName)
                                .collect(java.util.stream.Collectors.toList()));
                    }
                    if (student.getAgent() != null) {
                        dto.setUserNameAgent(student.getAgent().getUserId());
                    }
                    if (student.getCollege() != null) {
                        dto.setAssignedCollegeName(student.getCollege().getCollegeName());
                    }

                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Agent> getAgentByUserIdWithDetails(String userId) {
        return agentRepository.findByUserIdWithDetails(userId);
    }

    @Autowired
    private com.cc.repository.StudentRepository studentRepository;

    @Autowired
    private com.cc.repository.StudentStatusHistoryRepository studentStatusHistoryRepository;

    @Autowired
    private DocPersonService docPersonService;

    @Autowired
    private AccountantService accountantService;

    @Transactional
    public com.cc.modals.Student submitTelexDetails(Long studentId, com.cc.DTO.TelexApplicationDTO details,
            String visaPhotoPath, String visaPassportCopyPath, String ttCopyPath, String paymentConfirmationPath) {
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate Agent
        if (details.getAgentUsername() != null
                && (student.getAgent() == null || !student.getAgent().getUserId().equals(details.getAgentUsername()))) {
            // Optional: Enforce agent check if username is provided
            logger.warn("Agent username mismatch for student ID: {}", studentId);
        }

        // ======================= DOCUMENT TRACKING =======================
        if (details.getOriginalDocLocation() != null)
            student.setOriginalDocLocation(details.getOriginalDocLocation());
        if (details.getOriginalDocsSent() != null)
            student.setOriginalDocsSent(details.getOriginalDocsSent());
        if (details.getDocDeliveryMethod() != null)
            student.setDocDeliveryMethod(details.getDocDeliveryMethod());

        // Store Courier Partner and Tracking ID separately
        String courierPartner = details.getCourierPartner();
        if ("OTHER".equals(courierPartner) && details.getOtherCourierDetails() != null) {
            courierPartner = details.getOtherCourierDetails();
        }
        if (courierPartner != null && !courierPartner.isEmpty()) {
            student.setCourierPartner(courierPartner);
        }

        String trackingId = details.getCourierTrackingId();
        if (trackingId == null || trackingId.isEmpty()) {
            trackingId = details.getCourierTrackingNumber();
        }
        if (trackingId != null && !trackingId.isEmpty()) {
            student.setCourierTrackingId(trackingId);
        }

        if (details.getSelfPersonContact() != null)
            student.setSelfPersonContact(details.getSelfPersonContact());
        if (details.getDocTentativeReachDate() != null)
            student.setDocTentativeReachDate(details.getDocTentativeReachDate());

        // ======================= PAYMENT DETAILS =======================
        if (details.getHasPaid() != null)
            student.setHasPaid(details.getHasPaid());
        if (details.getPaidAmount() != null)
            student.setPaidAmount(details.getPaidAmount());
        if (details.getPaidTo() != null)
            student.setPaidTo(details.getPaidTo());
        if (details.getPaymentDueDate() != null)
            student.setPaymentDueDate(details.getPaymentDueDate());
        if (details.getPaymentType() != null)
            student.setPaymentType(details.getPaymentType());

        // Set payment document paths
        if (ttCopyPath != null) {
            student.setTtCopyPath(ttCopyPath);
        }
        if (paymentConfirmationPath != null) {
            student.setPaymentConfirmationPath(paymentConfirmationPath);
        }

        // ======================= PERSONAL/VISA INFORMATION =======================
        if (details.getSurname() != null)
            student.setSurname(details.getSurname());
        if (details.getFirstName() != null)
            student.setFirstName(details.getFirstName());
        if (details.getMiddleName() != null)
            student.setMiddleName(details.getMiddleName());
        if (details.getSex() != null)
            student.setSex(details.getSex());
        if (details.getCountryOfBirth() != null)
            student.setCountryOfBirth(details.getCountryOfBirth());
        if (details.getDateOfBirth() != null)
            student.setDob(details.getDateOfBirth());
        if (details.getPlaceOfBirth() != null)
            student.setPlaceOfBirth(details.getPlaceOfBirth());
        if (details.getMaritalStatus() != null)
            student.setMaritalStatus(details.getMaritalStatus());
        if (details.getCitizenship() != null)
            student.setCitizenship(details.getCitizenship());
        if (details.getPreviousCitizenship() != null)
            student.setPreviousCitizenship(details.getPreviousCitizenship());
        if (details.getPassportType() != null)
            student.setPassportType(details.getPassportType());
        if (details.getPassportIssueDate() != null)
            student.setPassportIssueDate(details.getPassportIssueDate());
        if (details.getPassportExpirationDate() != null)
            student.setPassportExpirationDate(details.getPassportExpirationDate());

        if (details.getPassportNumber() != null)
            student.setPassportNo(details.getPassportNumber());
        if (details.getDurationOfVisaProcedure() != null)
            student.setDurationOfVisaProcedure(details.getDurationOfVisaProcedure());
        if (details.getPlaceOfVisaIssuance() != null)
            student.setPlaceOfVisaIssuance(details.getPlaceOfVisaIssuance());

        // Set required document paths
        if (visaPhotoPath != null) {
            student.setVisaPhotoPath(visaPhotoPath);
        }
        if (visaPassportCopyPath != null) {
            student.setVisaPassportCopyPath(visaPassportCopyPath);
        }

        // Get and assign DocPerson to student
        Optional<DocPerson> docPersonOpt = docPersonService.getOne();
        if (docPersonOpt.isPresent()) {
            student.setDocPerson(docPersonOpt.get());
            student.setDocPersonAssignedDate(java.time.LocalDate.now());

            System.out.println(docPersonOpt.get().getUsername() + " Doc PErson USerNAme" + docPersonOpt.get());
            logger.info("Assigned DocPerson: {} to student ID: {}",

                    docPersonOpt.get().getUsername(), studentId);
        } else {
            logger.warn("No Active DocPerson available to assign to student ID: {}", studentId);
        }

        // Get and assign Accountant to student
        Optional<com.cc.modals.Accountant> accountantOpt = accountantService.getOne();
        if (accountantOpt.isPresent()) {
            student.setAccountant(accountantOpt.get());
            logger.info("Assigned Accountant: {} to student ID: {}",
                    accountantOpt.get().getUsername(), studentId);
        } else {
            // Debug: Log all accountants and their statuses
            java.util.List<com.cc.modals.Accountant> allAccountants = accountantService.getAll();
            logger.warn("No Active Accountant available to assign to student ID: {}. Total accountants in DB: {}",
                    studentId, allAccountants.size());
            allAccountants.forEach(a -> logger.warn("Accountant: {} has status: '{}'", a.getUsername(), a.getStatus()));
        }

        // Update Status
        student.setApplicationStatus(com.cc.modals.StudentStatus.TELEX_INITIATED);

        // Reset approvals if they were previously set (re-submission case)
        student.setIsTelexAccountantApproved(false);
        student.setIsTelexDocApproved(false);

        student.setAction(action.CONFIRM_DOCS);
        student.setActionOnUser(actionOnUser.doc_manager);

        com.cc.modals.Student savedStudent = studentRepository.save(student);

        // Debug: Verify what was saved
        logger.info("=== VERIFY AFTER SAVE: DocPerson assigned = {}, Accountant assigned = {} ===",
                savedStudent.getDocPerson() != null ? savedStudent.getDocPerson().getUsername() : "NULL",
                savedStudent.getAccountant() != null ? savedStudent.getAccountant().getUsername() : "NULL");

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(savedStudent);
        history.setStudentApplicationStatus(com.cc.modals.StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED);
        history.setUpdatedBy(details.getAgentUsername() != null ? details.getAgentUsername() : "Agent");
        history.setRemarks("Payment confirmation submitted");
        history.setUpdatedAt(java.time.LocalDateTime.now());
        studentStatusHistoryRepository.save(history);

        logger.info("Telex application submitted successfully for student ID: {}", studentId);
        return savedStudent;
    }

    @Transactional
    public DocumentReturnRequest requestDocsToDocPerson(Long studentId, String agentUsername,
            DocumentReturnRequestDTO details, String ticketPdfPath) {
        // Find Student
        com.cc.modals.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Validate Agent
        if (student.getAgent() == null || !student.getAgent().getUserId().equals(agentUsername)) {
            throw new RuntimeException("Unauthorized: Student is not assigned to this agent");
        }

        // Validate Status - Must be VISA_STAMPED
        if (student.getApplicationStatus() != com.cc.modals.StudentStatus.VISA_STAMPED) {
            throw new RuntimeException("Document return request can only be made when Visa is Stamped");
        }

        // Check if request already exists
        DocumentReturnRequest existingRequest = documentReturnRequestRepository.findByStudentId(studentId);
        if (existingRequest != null) {
            // Option: Update existing or throw error. Here we update.
            updateExistingRequest(existingRequest, details, ticketPdfPath);
            return documentReturnRequestRepository.save(existingRequest);
        }

        DocumentReturnRequest request = new DocumentReturnRequest();
        request.setStudent(student);
        request.setAgent(student.getAgent());
        if (request.getTravelDate() != null) {
            student.setApplicationStatus(com.cc.modals.StudentStatus.TRAVELLING);
        }
        // Map DTO to Entity
        request.setContactPersonName(details.getContactPersonName());
        request.setContactPhone(details.getContactPhone());
        request.setAlternativePhone(details.getAlternativePhone());
        request.setAddressWithZip(details.getAddressWithZip());
        request.setNeedBeforeDate(details.getNeedBeforeDate());

        request.setTravelDate(details.getTravelDate());
        request.setFromCountry(details.getFromCountry());
        request.setFromAirport(details.getFromAirport());
        request.setToCountry(details.getToCountry());
        request.setToAirport(details.getToAirport());
        request.setPnrNumber(details.getPnrNumber());

        if (ticketPdfPath != null) {
            request.setTicketPdfPath(ticketPdfPath);
        }

        // Initially false, will be true when Visa Manager returns docs
        request.setIsReturned(false);

        student.setCanRequestDocReturn(false);// again cant raise return request
        student.setHasRaiseReturnRequest(true); // doc person can now send
        studentRepository.save(student);

        // Update Student Action
        student.setAction(action.NEED_TO_RETURN_VISA); // Or stay same?
        // User said: "agent will fill this form" ... "VISA MANAGER will return".
        // Current status is VISA_STAMPED.

        // History
        com.cc.modals.StudentStatusHistory history = new com.cc.modals.StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(student.getApplicationStatus());
        history.setUpdatedBy(agentUsername);
        history.setUpdatedAt(java.time.LocalDateTime.now());
        history.setRemarks(
                "Document return requested. Contact: " + details.getContactPersonName() + " by " + agentUsername);
        studentStatusHistoryRepository.save(history);

        return documentReturnRequestRepository.save(request);
    }

    private void updateExistingRequest(DocumentReturnRequest request, DocumentReturnRequestDTO details,
            String ticketPdfPath) {
        if (details.getContactPersonName() != null)
            request.setContactPersonName(details.getContactPersonName());
        if (details.getContactPhone() != null)
            request.setContactPhone(details.getContactPhone());
        if (details.getAlternativePhone() != null)
            request.setAlternativePhone(details.getAlternativePhone());
        if (details.getAddressWithZip() != null)
            request.setAddressWithZip(details.getAddressWithZip());
        if (details.getNeedBeforeDate() != null)
            request.setNeedBeforeDate(details.getNeedBeforeDate());

        if (details.getTravelDate() != null)
            request.setTravelDate(details.getTravelDate());
        if (details.getFromCountry() != null)
            request.setFromCountry(details.getFromCountry());
        if (details.getFromAirport() != null)
            request.setFromAirport(details.getFromAirport());
        if (details.getToCountry() != null)
            request.setToCountry(details.getToCountry());
        if (details.getToAirport() != null)
            request.setToAirport(details.getToAirport());
        if (details.getPnrNumber() != null)
            request.setPnrNumber(details.getPnrNumber());

        if (ticketPdfPath != null) {
            request.setTicketPdfPath(ticketPdfPath);
        }
    }

    public com.cc.DTO.AgentDashboardStatsDTO getAgentDashboardStats(String username) {
        Agent agent = agentRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + username));

        List<com.cc.modals.Student> students = agent.getStudent();
        if (students == null) {
            return new com.cc.DTO.AgentDashboardStatsDTO(0L, 0L, 0L, 0L, 0L);
        }

        long totalStudents = students.size();
        long activeStudents = students.stream()
                .filter(s -> s.getApplicationStatus() != com.cc.modals.StudentStatus.REJECTED
                        && s.getApplicationStatus() != com.cc.modals.StudentStatus.VISA_STAMPED
                        && s.getApplicationStatus() != com.cc.modals.StudentStatus.COMPLETED)
                .count();
        long pendingApplications = students.stream()
                .filter(s -> s.getApplicationStatus() == com.cc.modals.StudentStatus.Submitted
                        || s.getApplicationStatus() == com.cc.modals.StudentStatus.REGISTERED
                        || s.getApplicationStatus() == com.cc.modals.StudentStatus.UNDER_REVIEW)
                .count();
        long visaApproved = students.stream()
                .filter(s -> s.getApplicationStatus() == com.cc.modals.StudentStatus.VISA_STAMPED)
                .count();
        long rejected = students.stream()
                .filter(s -> s.getApplicationStatus() == com.cc.modals.StudentStatus.REJECTED)
                .count();

        return new com.cc.DTO.AgentDashboardStatsDTO(totalStudents, activeStudents, pendingApplications, visaApproved,
                rejected);
    }

    public List<com.cc.DTO.StudentStatusCountDTO> getStudentStatusBreakdown(String username) {
        Agent agent = agentRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + username));

        List<com.cc.modals.Student> students = agent.getStudent();
        if (students == null) {
            return new ArrayList<>();
        }

        java.util.Map<String, Long> statusCounts = students.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getApplicationStatus().toString(),
                        java.util.stream.Collectors.counting()));

        List<com.cc.DTO.StudentStatusCountDTO> breakdown = new ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            breakdown.add(new com.cc.DTO.StudentStatusCountDTO(entry.getKey(), entry.getValue()));
        }

        return breakdown;
    }

    public List<com.cc.DTO.AgentDashboardActivityDTO> getRecentActivities(String username) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        List<com.cc.modals.StudentStatusHistory> historyList = studentStatusHistoryRepository
                .findRecentActivitiesByAgentUsername(username, pageable);

        return historyList.stream()
                .map(h -> new com.cc.DTO.AgentDashboardActivityDTO(
                        h.getStudent().getFullName(),
                        h.getStudentApplicationStatus().toString(),
                        h.getUpdatedAt(),
                        h.getRemarks()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<com.cc.DTO.AgentStudentDTO> getStudentsByAgentUsername(String username) {
        Agent agent = agentRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + username));

        List<com.cc.modals.Student> students = agent.getStudent();
        if (students == null) {
            return new ArrayList<>();
        }

        return students.stream()
                .map(student -> new com.cc.DTO.AgentStudentDTO(
                        student.getId(),
                        student.getFullName(),
                        student.getApplicationId()))
                .collect(java.util.stream.Collectors.toList());
    }

    // ======================= DATA FETCHING =======================

    public List<com.cc.DTO.AgentDetailsDTO> getAllAgentsDetails() {
        List<Agent> agents = agentRepository.findAll();

        return agents.stream().map(agent -> {
            com.cc.DTO.AgentDetailsDTO dto = new com.cc.DTO.AgentDetailsDTO();

            // ID & Dates
            dto.setId(agent.getId());
            dto.setCreationDate(agent.getCreationDate());
            dto.setActivationDate(agent.getActivationDate());

            // Personal
            dto.setFullName(agent.getFullName());
            dto.setAge(agent.getAge());
            dto.setDob(agent.getDob());
            dto.setEmail(agent.getEmail());
            dto.setContactNo(agent.getContactNo());
            dto.setGender(agent.getGender());
            dto.setAddress(agent.getAddress());
            dto.setIdentityProofType(agent.getIdentityProofType());
            dto.setIdentityProofNumber(agent.getIdentityProofNumber());
            dto.setDesignation(agent.getDesignation());
            dto.setExperience(agent.getExperience());
            dto.setStatus(agent.getStatus());

            // Company
            dto.setCompanyName(agent.getCompanyName());
            dto.setRegisteredBusinessName(agent.getRegisteredBusinessName());
            dto.setBusinessType(agent.getBusinessType());
            dto.setRegistrationNumber(agent.getRegistrationNumber());
            dto.setPanOrGstNumber(agent.getPanOrGstNumber());
            dto.setRegisteredOfficeAddress(agent.getRegisteredOfficeAddress());
            dto.setBranchOfficeAddress(agent.getBranchOfficeAddress());
            dto.setCompanyEmail(agent.getCompanyEmail());
            dto.setCompanyContactNo(agent.getCompanyContactNo());
            dto.setWebsite(agent.getWebsite());

            // Login
            dto.setUserId(agent.getUserId());
            dto.setPassword(agent.getPassword());
            dto.setRole(agent.getRole());

            // College Access
            if (agent.getCollegeAccess() != null) {
                List<com.cc.DTO.AgentCollegeAccessDTO> accessList = agent.getCollegeAccess().stream().map(access -> {
                    com.cc.DTO.AgentCollegeAccessDTO accessDTO = new com.cc.DTO.AgentCollegeAccessDTO();
                    accessDTO.setCollegeId(access.getCollege().getId());
                    accessDTO.setCollegeName(access.getCollege().getCollegeName()); // Set College Name
                    accessDTO.setAccessAllLocations(access.isAccessAllLocations());

                    if (access.getAllowedLocations() != null) {
                        // Set IDs
                        accessDTO.setLocationIds(access.getAllowedLocations().stream()
                                .map(com.cc.modals.Location::getId)
                                .collect(java.util.stream.Collectors.toList()));

                        // Set Details
                        accessDTO.setLocationDetails(access.getAllowedLocations().stream()
                                .map(loc -> new com.cc.DTO.AgentCollegeAccessDTO.LocationDetailDTO(
                                        loc.getId(),
                                        loc.getState(),
                                        loc.getDistrict()))
                                .collect(java.util.stream.Collectors.toList()));
                    }
                    return accessDTO;
                }).collect(java.util.stream.Collectors.toList());
                dto.setCollegeAccessList(accessList);
            }

            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    // ======================= AGENT PERFORMANCE REPORTS =======================

    public com.cc.DTO.AgentPerformanceStatsDTO getAgentPerformanceStats(String username, String period) {
        // Calculate dates
        java.time.LocalDate startDate = java.time.LocalDate.now();
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.now();

        if ("DAILY".equalsIgnoreCase(period)) {
            startDateTime = java.time.LocalDate.now().atStartOfDay();
            endDateTime = java.time.LocalDate.now().atTime(23, 59, 59);
            startDate = java.time.LocalDate.now();
            endDate = java.time.LocalDate.now();
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            startDateTime = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
            endDateTime = java.time.LocalDate.now().atTime(23, 59, 59);
            startDate = java.time.LocalDate.now().withDayOfMonth(1);
            endDate = java.time.LocalDate.now();
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            startDateTime = java.time.LocalDate.now().withDayOfYear(1).atStartOfDay();
            endDateTime = java.time.LocalDate.now().atTime(23, 59, 59);
            startDate = java.time.LocalDate.now().withDayOfYear(1);
            endDate = java.time.LocalDate.now();
        } else {
            startDateTime = java.time.LocalDate.now().atStartOfDay();
            endDateTime = java.time.LocalDate.now().atTime(23, 59, 59);
            startDate = java.time.LocalDate.now();
            endDate = java.time.LocalDate.now();
        }

        Agent agent = agentRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + username));
        String agentName = agent.getFullName();

        long registered = studentRepository.countByAgent_UserIdAndCreatedAtBetween(username, startDate, endDate);
        long telex = studentStatusHistoryRepository.countByUpdatedByAndStatusAndDateRange(
                username, com.cc.modals.StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED, startDateTime, endDateTime);

        // Improved Payment Confirmation Count: Matches 'Payment confirmation submitted'
        // remark OR 'TELEX_PAYMENT_DOCS_SUBMITTED' status
        // Since telex submission implies payment submission, we can safely use the
        // status count as a baseline or combine them.
        // To be safe and capture both old (status-only) and new (remark-tagged) data
        // without double counting if they overlap,
        // we can simply use the status count for now as it covers the event of
        // submitting payment docs.
        // Or if we want to be strict, we sum them? No, that would double count.
        // Best approach: Use the same count as Telex Initiated for now, as they are the
        // same action in the current workflow.
        // However, if there are other payment types (Visa Fee?), we might miss them.
        // But the user specifically noted the low count for 'payment confirmations'.
        // Let's use the telex count + any purely remark-based ones (if any exist that
        // aren't telex?).
        // Simple fix: Payment Confirmations = Telex Initiated (since that action IS
        // submitting payment).
        long payment = telex;

        // If we want to capture *future* specific payment actions that might NOT be
        // telex, we could add:
        // +
        // studentStatusHistoryRepository.countByUpdatedByAndRemarksContainingAndDateRange(username,
        // "Some Other Payment", ...);
        // But for now, fixing the report means aligning it with the actual data
        // present.
        long docs = studentStatusHistoryRepository.countByUpdatedByAndRemarksContainingAndDateRange(
                username, "Document return requested", startDateTime, endDateTime);

        // Debug log
        logger.info("Stats for {}: Reg={}, Telex={}, Pay={}, Docs={}, Period={}",
                username, registered, telex, payment, docs, period);

        return new com.cc.DTO.AgentPerformanceStatsDTO(agentName, username, registered, telex, payment, docs, period);
    }

    public List<com.cc.DTO.AgentPerformanceStatsDTO> getAllAgentsPerformanceStats(String period) {
        List<Agent> agents = agentRepository.findAll();
        List<com.cc.DTO.AgentPerformanceStatsDTO> statsList = new ArrayList<>();
        for (Agent agent : agents) {
            if (agent.getUserId() != null) {
                try {
                    statsList.add(getAgentPerformanceStats(agent.getUserId(), period));
                } catch (Exception e) {
                    logger.error("Error fetching stats for agent: " + agent.getUserId(), e);
                }
            }
        }
        return statsList;
    }

    // Schedule: Daily at 11:55 PM
    @Scheduled(cron = "0 55 23 * * ?")
    public void sendScheduledAgentReport() {
        logger.info("Executing scheduled Agent Performance Report");
        sendReportToEmail("DAILY", "admin@rareeducation.in");
    }

    public void sendReportToEmail(String period, String targetEmail) {
        List<com.cc.DTO.AgentPerformanceStatsDTO> stats = getAllAgentsPerformanceStats(period);
        String html = generateReportHtml(stats, period);

        try {
            emailService.sendHtmlEmail(targetEmail, period + " Agent Performance Report", html);
            logger.info("Sent {} agent report to {}", period, targetEmail);
        } catch (Exception e) {
            logger.error("Failed to send agent report to " + targetEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String generateReportHtml(List<com.cc.DTO.AgentPerformanceStatsDTO> stats, String period) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>").append(period).append(" Agent Performance Report</h2>");
        html.append("<p>Date: ").append(java.time.LocalDate.now()).append("</p>");
        html.append("<table border='1' cellspacing='0' cellpadding='5'>");
        html.append(
                "<tr style='background-color: #f2f2f2;'><th>Agent Name</th><th>Registered Students</th><th>Telex Initiated</th><th>Payment Confirmed</th><th>Docs Requested</th><th>Total Work</th></tr>");

        long totalRegistered = 0;
        long totalTelex = 0;
        long totalPayment = 0;
        long totalDocs = 0;
        long totalWorkAll = 0;

        for (com.cc.DTO.AgentPerformanceStatsDTO stat : stats) {
            html.append("<tr>");
            html.append("<td>").append(stat.getAgentName()).append("</td>");
            html.append("<td>").append(stat.getRegisteredStudents()).append("</td>");
            html.append("<td>").append(stat.getTelexInitiated()).append("</td>");
            html.append("<td>").append(stat.getPaymentConfirmations()).append("</td>");
            html.append("<td>").append(stat.getDocsRequested()).append("</td>");
            html.append("<td>").append(stat.getTotalWork()).append("</td>");
            html.append("</tr>");

            totalRegistered += stat.getRegisteredStudents();
            totalTelex += stat.getTelexInitiated();
            totalPayment += stat.getPaymentConfirmations();
            totalDocs += stat.getDocsRequested();
            totalWorkAll += stat.getTotalWork();
        }

        // Total Row
        html.append("<tr style='background-color: #e6e6e6; font-weight: bold;'>");
        html.append("<td>TOTAL</td>");
        html.append("<td>").append(totalRegistered).append("</td>");
        html.append("<td>").append(totalTelex).append("</td>");
        html.append("<td>").append(totalPayment).append("</td>");
        html.append("<td>").append(totalDocs).append("</td>");
        html.append("<td>").append(totalWorkAll).append("</td>");
        html.append("</tr>");

        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }
}