package com.cc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cc.DTO.UniversityPersonRequest;
import com.cc.modals.College;
import com.cc.modals.UniversityPerson;
import com.cc.repository.CollegeRepository;
import com.cc.repository.UniversityPersonRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.modals.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UniversityPersonService {

    private final UniversityPersonRepository upRepository;
    private final CollegeRepository collegeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentStatusHistoryRepository statusHistoryRepo;

    @Autowired
    private S3StorageService s3StorageService;

    public UniversityPersonService(UniversityPersonRepository upRepository,
            CollegeRepository collegeRepository) {
        this.upRepository = upRepository;
        this.collegeRepository = collegeRepository;
    }

    // -------------------- CREATE --------------------

    public UniversityPerson create(UniversityPersonRequest request) {

        if (upRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already taken");
        }

        // Check if this college is already assigned to an ACTIVE UniversityPerson
        java.util.Optional<UniversityPerson> existingAssignment = upRepository.findByCollegeId(request.getCollegeId());
        if (existingAssignment.isPresent()) {
            UniversityPerson assignedPerson = existingAssignment.get();
            if ("Active".equalsIgnoreCase(assignedPerson.getStatus())) {
                throw new RuntimeException("This college is already assigned to an active UniversityPerson: "
                        + assignedPerson.getUserName()
                        + ". Please deactivate them first or choose a different college.");
            }
        }

        // Fetch College
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        // Build Entity
        UniversityPerson person = new UniversityPerson();
        person.setUserName(request.getUserName());
        person.setPassword(request.getPassword());
        person.setStatus(request.getStatus());
        person.setRole(request.getRole());
        person.setCollege(college);
        User user = new User();

        user.setUsername(request.getUserName());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        userService.createUser(user);

        return upRepository.save(person);
    }

    // -------------------- UPDATE --------------------

    public UniversityPerson update(Long id, UniversityPersonRequest request) {

        UniversityPerson existing = upRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // Update fields
        existing.setUserName(request.getUserName());
        existing.setPassword(request.getPassword());
        existing.setStatus(request.getStatus());
        existing.setRole(request.getRole());

        if (request.getCollegeId() != null) {
            // Check if this college is already assigned to another ACTIVE UniversityPerson
            java.util.Optional<UniversityPerson> existingAssignment = upRepository
                    .findByCollegeId(request.getCollegeId());

            if (existingAssignment.isPresent()) {
                UniversityPerson assignedPerson = existingAssignment.get();
                // Only block if it's a DIFFERENT person AND that person is ACTIVE
                if (!assignedPerson.getId().equals(id) && "Active".equalsIgnoreCase(assignedPerson.getStatus())) {
                    throw new RuntimeException("This college is already assigned to an active UniversityPerson: "
                            + assignedPerson.getUserName()
                            + ". Please deactivate them first or choose a different college.");
                }
            }

            College college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("College not found"));
            existing.setCollege(college);
        }

        return upRepository.save(existing);
    }

    // -------------------- READ --------------------

    public UniversityPerson getById(Long id) {
        return upRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    public List<UniversityPerson> getAll() {
        return upRepository.findAll();
    }

    public UniversityPerson getByUserName(String userName) {
        return upRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("UniversityPerson not found with username: " + userName));
    }

    // -------------------- DELETE --------------------

    public void delete(Long id) {
        upRepository.deleteById(id);
    }

    public boolean isUserNameAvailable(String userName) {
        return !upRepository.existsByUserName(userName);
    }

    // -------------------- ADMISSION LETTER UPLOAD --------------------

    @org.springframework.transaction.annotation.Transactional
    public Student uploadAdmissionLetter(Long studentId, String universityPersonUsername, MultipartFile file)
            throws IOException {

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Find UniversityPerson by username
        UniversityPerson universityPerson = upRepository.findByUserName(universityPersonUsername)
                .orElseThrow(() -> new RuntimeException(
                        "UniversityPerson not found with username: " + universityPersonUsername));

        // Validate that this student is assigned to this UniversityPerson
        if (student.getUniversityPerson() == null ||
                !student.getUniversityPerson().getId().equals(universityPerson.getId())) {
            throw new RuntimeException("Student is not assigned to this UniversityPerson");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Save file using S3StorageService
        String filePath = s3StorageService.uploadFile(file, "admission-letters", "admission_letter_" + student.getId());

        // Update student
        student.setAdmissionLetterPath(filePath);
        // update action
        student.setAction(action.APPROVE_ADMISSION_LETTER);
        student.setActionOnUser(actionOnUser.admission_manager);

        student.setApplicationStatus(StudentStatus.PENDING_ADMISSION_LETTER);
        Student updatedStudent = studentRepository.save(student);

        // Create status history entry
        StudentStatusHistory historyEntry = new StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.PENDING_ADMISSION_LETTER);
        historyEntry.setUpdatedBy(universityPersonUsername);
        historyEntry.setUpdatedByName(userService.resolveFullName(universityPersonUsername));
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- CONDITIONAL OFFER LETTER UPLOAD --------------------

    @org.springframework.transaction.annotation.Transactional
    public Student uploadConditionalOfferLetter(Long studentId, String universityPersonUsername, MultipartFile file)
            throws IOException {

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Find UniversityPerson by username
        UniversityPerson universityPerson = upRepository.findByUserName(universityPersonUsername)
                .orElseThrow(() -> new RuntimeException(
                        "UniversityPerson not found with username: " + universityPersonUsername));

        // Validate that this student is assigned to this UniversityPerson
        if (student.getUniversityPerson() == null ||
                !student.getUniversityPerson().getId().equals(universityPerson.getId())) {
            throw new RuntimeException("Student is not assigned to this UniversityPerson");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Save file using S3StorageService
        String filePath = s3StorageService.uploadFile(file, "conditional-letters",
                "conditional_letter_" + student.getId());

        // update action
        student.setAction(action.UPLOAD_ADMISSION_LETTER);
        student.setActionOnUser(actionOnUser.UniversityCoordinator);

        // Update student
        student.setConditionalOfferLetterPath(filePath);
        student.setApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
        Student updatedStudent = studentRepository.save(student);

        // Create status history entry
        StudentStatusHistory historyEntry = new StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
        historyEntry.setUpdatedBy(universityPersonUsername);
        historyEntry.setUpdatedByName(userService.resolveFullName(universityPersonUsername));
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- VISA TALAB NUMA UPLOAD --------------------

    @org.springframework.transaction.annotation.Transactional
    public Student uploadVisaTalabNuma(Long studentId, String universityPersonUsername, MultipartFile file)
            throws IOException {

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Find UniversityPerson by username
        UniversityPerson universityPerson = upRepository.findByUserName(universityPersonUsername)
                .orElseThrow(() -> new RuntimeException(
                        "UniversityPerson not found with username: " + universityPersonUsername));

        // Validate that this student is assigned to this UniversityPerson
        if (student.getUniversityPerson() == null ||
                !student.getUniversityPerson().getId().equals(universityPerson.getId())) {
            throw new RuntimeException("Student is not assigned to this UniversityPerson");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Save file using S3StorageService
        String filePath = s3StorageService.uploadFile(file, "visa-talab-numa",
                "visa_talab_numa_" + student.getId());

        // Update student
        student.setVisaTalabNumaPath(filePath);
        Student updatedStudent = studentRepository.save(student);

        // Create status history entry
        StudentStatusHistory historyEntry = new StudentStatusHistory();
        historyEntry.setStudent(updatedStudent);
        historyEntry.setStudentApplicationStatus(student.getApplicationStatus());
        historyEntry.setRemarks("Uploaded Visa Talab Numa Document");
        historyEntry.setUpdatedBy(universityPersonUsername);
        historyEntry.setUpdatedByName(userService.resolveFullName(universityPersonUsername));
        historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
        statusHistoryRepo.save(historyEntry);

        return updatedStudent;
    }

    // -------------------- DASHBOARD METHODS --------------------

    public com.cc.DTO.UniversityDashboardStatsDTO getDashboardStats(String username) {
        UniversityPerson person = getByUserName(username);
        List<Student> students = person.getStudents();

        if (students == null || students.isEmpty()) {
            return new com.cc.DTO.UniversityDashboardStatsDTO(0L, 0L, 0L, 0L);
        }

        long totalStudents = students.size();
        long admittedStudents = studentRepository.findAdmittedStudentsByUniversityPerson(username).size();
        long rejectedStudents = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REJECTED)
                .count();
        long pendingApplications = totalStudents - admittedStudents - rejectedStudents;

        return new com.cc.DTO.UniversityDashboardStatsDTO(totalStudents, admittedStudents, pendingApplications,
                rejectedStudents);
    }

    public List<com.cc.DTO.UniversityStudentStatusCountDTO> getStudentStatusBreakdown(String username) {
        UniversityPerson person = getByUserName(username);
        List<Student> students = person.getStudents();

        if (students == null || students.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.Map<String, Long> statusCounts = students.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getApplicationStatus().toString(),
                        java.util.stream.Collectors.counting()));

        List<com.cc.DTO.UniversityStudentStatusCountDTO> breakdown = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            breakdown.add(new com.cc.DTO.UniversityStudentStatusCountDTO(entry.getKey(), entry.getValue()));
        }

        return breakdown;
    }

    public List<com.cc.DTO.AdmittedStudentDTO> getAdmittedStudents(String username) {
        List<Student> admittedStudents = studentRepository.findAdmittedStudentsByUniversityPerson(username);

        return admittedStudents.stream()
                .map(s -> new com.cc.DTO.AdmittedStudentDTO(
                        s.getId(),
                        s.getFullName(),
                        s.getPassportNo(),
                        s.getAppliedFor(),
                        s.getApplicationStatus().toString(),
                        java.time.LocalDateTime.now() // Placeholder
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== PERFORMANCE REPORTS ====================

    @Autowired
    private EmailService emailService;

    public com.cc.DTO.UniversityPersonReportDTO generateReport(Long upId, String period) {
        UniversityPerson up = upRepository.findById(upId)
                .orElseThrow(() -> new RuntimeException("UniversityPerson not found with ID: " + upId));
        return generateReportForPerson(up, period);
    }

    public List<com.cc.DTO.UniversityPersonReportDTO> generateAllReports(String period) {
        List<UniversityPerson> allUps = upRepository.findAll();
        return allUps.stream()
                .filter(up -> "Active".equalsIgnoreCase(up.getStatus()))
                .map(up -> generateReportForPerson(up, period))
                .collect(java.util.stream.Collectors.toList());
    }

    private com.cc.DTO.UniversityPersonReportDTO generateReportForPerson(UniversityPerson up, String period) {
        if (up.getCollege() == null) {
            // No college assigned, return empty stats
            return new com.cc.DTO.UniversityPersonReportDTO(
                    up.getId(), up.getUserName(), "No College Assigned", null, period,
                    new com.cc.DTO.UniversityPersonReportDTO.UniversityPersonStatsDTO());
        }

        Long universityPersonId = up.getId();
        String collegeName = up.getCollege().getCollegeName();
        Long collegeId = up.getCollege().getId();

        // Determine date ranges
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "yearly":
                currentStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                currentEnd = now;
                previousStart = currentStart.minusYears(1);
                previousEnd = currentEnd.minusYears(1);
                break;
            case "monthly":
                currentStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                currentEnd = now;
                previousStart = currentStart.minusMonths(1);
                previousEnd = currentEnd.minusMonths(1);
                break;
            case "daily":
            default:
                currentStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                currentEnd = now;
                previousStart = currentStart.minusDays(1);
                previousEnd = currentEnd.minusDays(1);
                break;
        }

        // Get history for students assigned to THIS UniversityPerson within date ranges
        List<StudentStatusHistory> currentHistory = statusHistoryRepo.findByUniversityPersonIdAndDateRange(
                universityPersonId, currentStart, currentEnd);
        List<StudentStatusHistory> previousHistory = statusHistoryRepo.findByUniversityPersonIdAndDateRange(
                universityPersonId, previousStart, previousEnd);

        // Count unique students per status
        var currentCounts = countUniqueStudentsByStatus(currentHistory);
        var previousCounts = countUniqueStudentsByStatus(previousHistory);

        // Get UNIQUE students who had activity in the CURRENT period (dynamic by
        // period)
        java.util.Set<Long> uniqueStudentIds = currentHistory.stream()
                .map(h -> h.getStudent().getId())
                .collect(java.util.stream.Collectors.toSet());
        int totalStudentsInPeriod = uniqueStudentIds.size();

        // Get unique previous period student count for trend calculation
        java.util.Set<Long> uniquePrevStudentIds = previousHistory.stream()
                .map(h -> h.getStudent().getId())
                .collect(java.util.stream.Collectors.toSet());
        int previousTotalStudents = uniquePrevStudentIds.size();

        // Build stats
        com.cc.DTO.UniversityPersonReportDTO.UniversityPersonStatsDTO stats = new com.cc.DTO.UniversityPersonReportDTO.UniversityPersonStatsDTO();
        stats.setTotalStudents(totalStudentsInPeriod);
        stats.setLettersUploaded(currentCounts.getOrDefault(StudentStatus.PENDING_ADMISSION_LETTER, 0));
        stats.setConditionalOffersUploaded(
                currentCounts.getOrDefault(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED, 0));
        stats.setPendingLetters(totalStudentsInPeriod - stats.getLettersUploaded());
        stats.setEfficiencyRate(
                totalStudentsInPeriod > 0 ? ((double) stats.getLettersUploaded() / totalStudentsInPeriod) * 100 : 0);

        // Calculate trends
        int prevLetters = previousCounts.getOrDefault(StudentStatus.PENDING_ADMISSION_LETTER, 0);
        int prevConditional = previousCounts.getOrDefault(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED, 0);

        stats.setLettersUploadedTrend(
                com.cc.DTO.UniversityPersonReportDTO.UniversityPersonStatsDTO.calculateTrend(
                        stats.getLettersUploaded(), prevLetters));
        stats.setConditionalOffersTrend(
                com.cc.DTO.UniversityPersonReportDTO.UniversityPersonStatsDTO.calculateTrend(
                        stats.getConditionalOffersUploaded(), prevConditional));

        return new com.cc.DTO.UniversityPersonReportDTO(up.getId(), up.getUserName(), collegeName, collegeId, period,
                stats);
    }

    private java.util.Map<StudentStatus, Integer> countUniqueStudentsByStatus(List<StudentStatusHistory> history) {
        java.util.Map<StudentStatus, java.util.Set<Long>> statusStudents = new java.util.HashMap<>();

        for (StudentStatusHistory entry : history) {
            StudentStatus status = entry.getStudentApplicationStatus();
            if (status != null) {
                statusStudents.computeIfAbsent(status, k -> new java.util.HashSet<>())
                        .add(entry.getStudent().getId());
            }
        }

        java.util.Map<StudentStatus, Integer> counts = new java.util.HashMap<>();
        for (java.util.Map.Entry<StudentStatus, java.util.Set<Long>> e : statusStudents.entrySet()) {
            counts.put(e.getKey(), e.getValue().size());
        }
        return counts;
    }

    // ==================== EMAIL REPORTS ====================

    public void sendReportByEmail(Long upId, String period, String recipientEmail) {
        com.cc.DTO.UniversityPersonReportDTO report = generateReport(upId, period);
        String htmlContent = generateReportHtml(report, period);
        String subject = "📊 University Coordinator Report - " + report.getCollegeName() + " ("
                + period.toUpperCase() + ")";
        emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);
    }

    public void sendAllReportsEmail(String period, String recipientEmail) {
        List<com.cc.DTO.UniversityPersonReportDTO> reports = generateAllReports(period);
        String htmlContent = generateAllReportsHtml(reports, period);
        String subject = "📊 All University Coordinators Report (" + period.toUpperCase() + ")";
        emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);
    }

    private String generateReportHtml(com.cc.DTO.UniversityPersonReportDTO report, String period) {
        var stats = report.getPeriodStats();
        String periodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

        String performanceRating;
        String performanceColor;
        if (stats.getEfficiencyRate() >= 80) {
            performanceRating = "🌟 Excellent";
            performanceColor = "#22c55e";
        } else if (stats.getEfficiencyRate() >= 60) {
            performanceRating = "👍 Good";
            performanceColor = "#f59e0b";
        } else if (stats.getEfficiencyRate() >= 40) {
            performanceRating = "⚡ Average";
            performanceColor = "#fb923c";
        } else {
            performanceRating = "📈 Needs Improvement";
            performanceColor = "#ef4444";
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                        .container { max-width: 650px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
                        .header { background: linear-gradient(135deg, #8b5cf6 0%%, #6366f1 100%%); padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 26px; color: #fff; font-weight: 700; }
                        .header p { margin: 8px 0 0; color: rgba(255,255,255,0.8); font-size: 14px; }
                        .content { padding: 30px; }
                        .performance-card { background: linear-gradient(135deg, rgba(139,92,246,0.15) 0%%, rgba(99,102,241,0.1) 100%%); border: 1px solid rgba(139,92,246,0.3); border-radius: 16px; padding: 20px; margin-bottom: 25px; text-align: center; }
                        .performance-rating { font-size: 28px; font-weight: 700; margin-bottom: 5px; }
                        .metrics-row { display: flex; justify-content: space-between; gap: 15px; margin-bottom: 20px; }
                        .metric-box { flex: 1; background: rgba(255,255,255,0.05); border-radius: 12px; padding: 18px; text-align: center; border: 1px solid rgba(255,255,255,0.1); }
                        .metric-value { font-size: 28px; font-weight: 700; color: #fff; }
                        .metric-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; text-transform: uppercase; }
                        .metric-trend { font-size: 11px; margin-top: 6px; padding: 3px 8px; border-radius: 10px; display: inline-block; }
                        .trend-positive { background: rgba(34,197,94,0.2); color: #22c55e; }
                        .trend-negative { background: rgba(239,68,68,0.2); color: #ef4444; }
                        .trend-neutral { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.5); }
                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎓 University Coordinator Report</h1>
                            <p>%s - %s</p>
                            <span style="display: inline-block; background: rgba(255,255,255,0.2); color: #fff; padding: 6px 16px; border-radius: 20px; font-size: 12px; margin-top: 12px;">%s Analysis</span>
                        </div>
                        <div class="content">
                            <div class="performance-card">
                                <div class="performance-rating" style="color: %s">%s</div>
                                <div style="color: rgba(255,255,255,0.6); font-size: 12px;">Overall Performance</div>
                            </div>
                            <div class="metrics-row">
                                <div class="metric-box">
                                    <div class="metric-value">%d</div>
                                    <div class="metric-label">Total Students</div>
                                </div>
                                <div class="metric-box">
                                    <div class="metric-value" style="color: #22c55e">%d</div>
                                    <div class="metric-label">Letters Uploaded</div>
                                    <span class="metric-trend %s">%s</span>
                                </div>
                                <div class="metric-box">
                                    <div class="metric-value" style="color: #f59e0b">%d</div>
                                    <div class="metric-label">Pending</div>
                                </div>
                            </div>
                            <div class="metrics-row">
                                <div class="metric-box">
                                    <div class="metric-value" style="color: #8b5cf6">%d</div>
                                    <div class="metric-label">Conditional Offers</div>
                                    <span class="metric-trend %s">%s</span>
                                </div>
                                <div class="metric-box">
                                    <div class="metric-value" style="color: #60a5fa">%.1f%%</div>
                                    <div class="metric-label">Efficiency Rate</div>
                                </div>
                            </div>
                        </div>
                        <div class="footer">
                            Generated on %s at %s | CampusConnect
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        report.getUserName(),
                        report.getCollegeName(),
                        periodLabel,
                        performanceColor,
                        performanceRating,
                        stats.getTotalStudents(),
                        stats.getLettersUploaded(),
                        getTrendClass(stats.getLettersUploadedTrend()),
                        formatTrend(stats.getLettersUploadedTrend()),
                        stats.getPendingLetters(),
                        stats.getConditionalOffersUploaded(),
                        getTrendClass(stats.getConditionalOffersTrend()),
                        formatTrend(stats.getConditionalOffersTrend()),
                        stats.getEfficiencyRate(),
                        java.time.LocalDate.now().toString(),
                        java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    private String generateAllReportsHtml(List<com.cc.DTO.UniversityPersonReportDTO> reports, String period) {
        String periodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

        int totalLetters = 0, totalStudents = 0;
        com.cc.DTO.UniversityPersonReportDTO topPerformer = null;
        double topEfficiency = -1;

        StringBuilder rows = new StringBuilder();
        for (var r : reports) {
            var s = r.getPeriodStats();
            totalLetters += s.getLettersUploaded();
            totalStudents += s.getTotalStudents();

            if (s.getEfficiencyRate() > topEfficiency) {
                topEfficiency = s.getEfficiencyRate();
                topPerformer = r;
            }

            String effColor = s.getEfficiencyRate() >= 70 ? "#22c55e"
                    : (s.getEfficiencyRate() >= 40 ? "#f59e0b" : "#ef4444");
            String isTop = (topPerformer != null && r.getId().equals(topPerformer.getId())) ? "🏆" : "";

            rows.append(String.format(
                    """
                            <tr style="background: rgba(255,255,255,0.02);">
                                <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); color: #fff;"><strong>%s</strong> %s</td>
                                <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.8);">%s</td>
                                <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #fff;">%d</td>
                                <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #22c55e;">%d</td>
                                <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center;"><span style="color: %s; font-weight: 600;">%.0f%%</span></td>
                            </tr>
                            """,
                    r.getUserName(), isTop, r.getCollegeName(),
                    s.getTotalStudents(), s.getLettersUploaded(),
                    effColor, s.getEfficiencyRate()));
        }

        double avgEfficiency = totalStudents > 0 ? ((double) totalLetters / totalStudents) * 100 : 0;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                        .container { max-width: 800px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; overflow: hidden; }
                        .header { background: linear-gradient(135deg, #8b5cf6 0%%, #6366f1 100%%); padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 26px; color: #fff; }
                        .summary-row { display: flex; padding: 25px 30px; gap: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); }
                        .summary-box { flex: 1; text-align: center; padding: 15px; background: rgba(255,255,255,0.05); border-radius: 12px; }
                        .summary-value { font-size: 28px; font-weight: 700; }
                        .summary-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; }
                        .content { padding: 25px 30px; }
                        table { width: 100%%; border-collapse: collapse; }
                        th { background: rgba(139,92,246,0.15); color: #8b5cf6; padding: 14px 12px; text-align: left; font-size: 12px; text-transform: uppercase; }
                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎓 All University Coordinators Report</h1>
                            <span style="display: inline-block; background: rgba(255,255,255,0.2); padding: 6px 16px; border-radius: 20px; font-size: 12px; margin-top: 12px;">%s Summary</span>
                        </div>
                        <div class="summary-row">
                            <div class="summary-box">
                                <div class="summary-value" style="color: #8b5cf6">%d</div>
                                <div class="summary-label">Coordinators</div>
                            </div>
                            <div class="summary-box">
                                <div class="summary-value" style="color: #22c55e">%d</div>
                                <div class="summary-label">Letters Uploaded</div>
                            </div>
                            <div class="summary-box">
                                <div class="summary-value" style="color: #60a5fa">%.1f%%</div>
                                <div class="summary-label">Avg Efficiency</div>
                            </div>
                        </div>
                        <div class="content">
                            %s
                            <table>
                                <thead>
                                    <tr>
                                        <th>Coordinator</th>
                                        <th>College</th>
                                        <th style="text-align: center;">Students</th>
                                        <th style="text-align: center;">Uploaded</th>
                                        <th style="text-align: center;">Efficiency</th>
                                    </tr>
                                </thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                        <div class="footer">
                            Generated on %s at %s | CampusConnect
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        periodLabel,
                        reports.size(),
                        totalLetters,
                        avgEfficiency,
                        topPerformer != null ? String.format(
                                """
                                        <div style="background: rgba(34,197,94,0.1); border: 1px solid rgba(34,197,94,0.3); border-radius: 12px; padding: 15px 20px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px;">
                                            <span style="font-size: 32px;">🏆</span>
                                            <div><h3 style="margin: 0; color: #22c55e; font-size: 16px;">Top Performer: %s</h3><p style="margin: 4px 0 0; color: rgba(255,255,255,0.6); font-size: 12px;">%.0f%% efficiency at %s</p></div>
                                        </div>
                                        """,
                                topPerformer.getUserName(), topEfficiency, topPerformer.getCollegeName()) : "",
                        rows.toString(),
                        java.time.LocalDate.now().toString(),
                        java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    private String getTrendClass(Double trend) {
        if (trend == null)
            return "trend-neutral";
        return trend >= 0 ? "trend-positive" : "trend-negative";
    }

    private String formatTrend(Double trend) {
        if (trend == null)
            return "N/A";
        return (trend >= 0 ? "+" : "") + String.format("%.1f", trend) + "%";
    }
}