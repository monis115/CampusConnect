package com.cc.service;

import com.cc.modals.College;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.UniversityPerson;
import com.cc.repository.CollegeRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.StudentStatusHistoryRepository;
import com.cc.repository.UniversityPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CollegeService {

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentStatusHistoryRepository statusHistoryRepo;

    @Autowired
    private UniversityPersonRepository universityPersonRepository;

    @Autowired
    private EmailService emailService;

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public Optional<College> getCollegeById(Long id) {
        return collegeRepository.findById(id);
    }

    public College createCollege(College college) {
        return collegeRepository.save(college);
    }

    public College updateCollege(Long id, College updatedCollege) {
        return collegeRepository.findById(id).map(college -> {
            college.setCollegeName(updatedCollege.getCollegeName());
            college.setCity(updatedCollege.getCity());
            college.setCountry(updatedCollege.getCountry());
            return collegeRepository.save(college);
        }).orElse(null);
    }

    public void deleteCollege(Long id) {
        collegeRepository.deleteById(id);
    }

    public College findByCollegeName(String collegeName) {
        return collegeRepository.findByCollegeName(collegeName);
    }

    public com.cc.DTO.UniversityCoordinatorDTO getUniversityCoordinator(Long collegeId) {
        return collegeRepository.findById(collegeId)
                .map(college -> {
                    UniversityPerson person = college.getUniversityPerson();
                    if (person == null) {
                        return null;
                    }
                    return new com.cc.DTO.UniversityCoordinatorDTO(
                            person.getId(),
                            person.getUserName(),
                            person.getStatus(),
                            person.getRole(),
                            college.getCollegeName());
                })
                .orElse(null);
    }

    // ==================== PERFORMANCE REPORTS ====================

    public com.cc.DTO.CollegeReportDTO generateCollegeReport(Long collegeId, String period) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found with ID: " + collegeId));
        return buildCollegeReport(college, period);
    }

    public List<com.cc.DTO.CollegeReportDTO> generateAllCollegesReport(String period) {
        return collegeRepository.findAll().stream()
                .map(college -> buildCollegeReport(college, period))
                .collect(java.util.stream.Collectors.toList());
    }

    private com.cc.DTO.CollegeReportDTO buildCollegeReport(College college, String period) {
        Long collegeId = college.getId();

        // Get coordinator info
        Optional<UniversityPerson> upOpt = universityPersonRepository.findByCollegeId(collegeId);
        com.cc.DTO.CollegeReportDTO.CoordinatorDTO coordinatorDTO = null;
        if (upOpt.isPresent()) {
            UniversityPerson up = upOpt.get();
            coordinatorDTO = new com.cc.DTO.CollegeReportDTO.CoordinatorDTO(
                    up.getId(), up.getUserName(), up.getStatus());
        }

        // Determine date ranges
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

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

        // Get history for this college within date ranges
        List<StudentStatusHistory> currentHistory = statusHistoryRepo.findByCollegeAndDateRange(
                collegeId, currentStart, currentEnd);
        List<StudentStatusHistory> previousHistory = statusHistoryRepo.findByCollegeAndDateRange(
                collegeId, previousStart, previousEnd);

        // Count unique students per status
        var currentCounts = countUniqueStudentsByStatus(currentHistory);
        var previousCounts = countUniqueStudentsByStatus(previousHistory);

        // Get UNIQUE students who had activity in the CURRENT period (dynamic by
        // period)
        java.util.Set<Long> uniqueStudentIds = currentHistory.stream()
                .map(h -> h.getStudent().getId())
                .collect(java.util.stream.Collectors.toSet());
        int totalStudentsInPeriod = uniqueStudentIds.size();

        // Build stats
        com.cc.DTO.CollegeReportDTO.CollegeStatsDTO stats = new com.cc.DTO.CollegeReportDTO.CollegeStatsDTO();
        stats.setTotalStudents(totalStudentsInPeriod);
        stats.setLettersUploaded(currentCounts.getOrDefault(StudentStatus.PENDING_ADMISSION_LETTER, 0));
        stats.setConditionalOffers(
                currentCounts.getOrDefault(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED, 0));
        stats.setPendingLetters(totalStudentsInPeriod - stats.getLettersUploaded());
        stats.setEfficiencyRate(
                totalStudentsInPeriod > 0 ? ((double) stats.getLettersUploaded() / totalStudentsInPeriod) * 100 : 0);

        // Calculate trends
        int prevLetters = previousCounts.getOrDefault(StudentStatus.PENDING_ADMISSION_LETTER, 0);
        int prevConditional = previousCounts.getOrDefault(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED, 0);
        stats.setLettersTrend(
                com.cc.DTO.CollegeReportDTO.CollegeStatsDTO.calculateTrend(stats.getLettersUploaded(), prevLetters));
        stats.setConditionalOffersTrend(
                com.cc.DTO.CollegeReportDTO.CollegeStatsDTO.calculateTrend(stats.getConditionalOffers(),
                        prevConditional));

        return new com.cc.DTO.CollegeReportDTO(
                collegeId, college.getCollegeName(), college.getCity(), college.getCountry(),
                period, coordinatorDTO, stats);
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

    public void sendCollegeReportByEmail(Long collegeId, String period, String email) {
        com.cc.DTO.CollegeReportDTO report = generateCollegeReport(collegeId, period);
        String html = generateCollegeReportHtml(report);
        String subject = "🏛️ University Report - " + report.getCollegeName() + " (" + period.toUpperCase() + ")";
        emailService.sendHtmlEmail(email, subject, html);
    }

    public void sendAllCollegesReportEmail(String period, String email) {
        List<com.cc.DTO.CollegeReportDTO> reports = generateAllCollegesReport(period);
        String html = generateAllCollegesReportHtml(reports, period);
        String subject = "🏛️ All Universities Report (" + period.toUpperCase() + ")";
        emailService.sendHtmlEmail(email, subject, html);
    }

    private String generateCollegeReportHtml(com.cc.DTO.CollegeReportDTO report) {
        var stats = report.getStats();
        String periodLabel = report.getPeriod().substring(0, 1).toUpperCase() + report.getPeriod().substring(1);
        String coordinatorInfo = report.getCoordinator() != null
                ? report.getCoordinator().getUserName() + " (" + report.getCoordinator().getStatus() + ")"
                : "Not Assigned";

        String performanceColor = stats.getEfficiencyRate() >= 70 ? "#22c55e"
                : (stats.getEfficiencyRate() >= 40 ? "#f59e0b" : "#ef4444");

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                        .container { max-width: 650px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; overflow: hidden; }
                        .header { background: linear-gradient(135deg, #0ea5e9 0%%, #0284c7 100%%); padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; color: #fff; }
                        .header p { margin: 8px 0 0; color: rgba(255,255,255,0.8); font-size: 14px; }
                        .content { padding: 30px; }
                        .info-row { display: flex; justify-content: space-between; background: rgba(255,255,255,0.05); padding: 15px 20px; border-radius: 12px; margin-bottom: 20px; }
                        .info-item { text-align: center; }
                        .info-label { font-size: 11px; color: rgba(255,255,255,0.5); text-transform: uppercase; }
                        .info-value { font-size: 14px; margin-top: 4px; }
                        .metrics-row { display: flex; gap: 15px; margin-bottom: 15px; }
                        .metric-box { flex: 1; background: rgba(255,255,255,0.05); border-radius: 12px; padding: 18px; text-align: center; border: 1px solid rgba(255,255,255,0.1); }
                        .metric-value { font-size: 28px; font-weight: 700; }
                        .metric-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; text-transform: uppercase; }
                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🏛️ %s</h1>
                            <p>%s, %s | %s Analysis</p>
                        </div>
                        <div class="content">
                            <div class="info-row">
                                <div class="info-item"><div class="info-label">Coordinator</div><div class="info-value">%s</div></div>
                                <div class="info-item"><div class="info-label">Efficiency</div><div class="info-value" style="color: %s">%.1f%%</div></div>
                            </div>
                            <div class="metrics-row">
                                <div class="metric-box"><div class="metric-value">%d</div><div class="metric-label">Total Students</div></div>
                                <div class="metric-box"><div class="metric-value" style="color: #22c55e">%d</div><div class="metric-label">Letters Uploaded</div></div>
                            </div>
                            <div class="metrics-row">
                                <div class="metric-box"><div class="metric-value" style="color: #8b5cf6">%d</div><div class="metric-label">Conditional Offers</div></div>
                                <div class="metric-box"><div class="metric-value" style="color: #f59e0b">%d</div><div class="metric-label">Pending</div></div>
                            </div>
                        </div>
                        <div class="footer">Generated on %s | CampusConnect</div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        report.getCollegeName(),
                        report.getCity(), report.getCountry(), periodLabel,
                        coordinatorInfo,
                        performanceColor, stats.getEfficiencyRate(),
                        stats.getTotalStudents(), stats.getLettersUploaded(),
                        stats.getConditionalOffers(), stats.getPendingLetters(),
                        java.time.LocalDate.now().toString());
    }

    private String generateAllCollegesReportHtml(List<com.cc.DTO.CollegeReportDTO> reports, String period) {
        String periodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

        int totalStudents = 0, totalLetters = 0;
        StringBuilder rows = new StringBuilder();

        for (var r : reports) {
            var s = r.getStats();
            totalStudents += s.getTotalStudents();
            totalLetters += s.getLettersUploaded();

            String coordinator = r.getCoordinator() != null ? r.getCoordinator().getUserName() : "—";
            String effColor = s.getEfficiencyRate() >= 70 ? "#22c55e"
                    : (s.getEfficiencyRate() >= 40 ? "#f59e0b" : "#ef4444");

            rows.append(String.format(
                    """
                            <tr style="background: rgba(255,255,255,0.02);">
                                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); color: #fff;"><strong>%s</strong><br><span style="font-size: 11px; color: rgba(255,255,255,0.5);">%s, %s</span></td>
                                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.8);">%s</td>
                                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #fff;">%d</td>
                                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #22c55e;">%d</td>
                                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center;"><span style="color: %s;">%.0f%%</span></td>
                            </tr>
                            """,
                    r.getCollegeName(), r.getCity(), r.getCountry(),
                    coordinator, s.getTotalStudents(), s.getLettersUploaded(),
                    effColor, s.getEfficiencyRate()));
        }

        double avgEfficiency = totalStudents > 0 ? ((double) totalLetters / totalStudents) * 100 : 0;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                        .container { max-width: 850px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; overflow: hidden; }
                        .header { background: linear-gradient(135deg, #0ea5e9 0%%, #0284c7 100%%); padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 24px; color: #fff; }
                        .summary { display: flex; padding: 25px; gap: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); }
                        .summary-box { flex: 1; text-align: center; padding: 15px; background: rgba(255,255,255,0.05); border-radius: 12px; }
                        .summary-value { font-size: 28px; font-weight: 700; }
                        .summary-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; }
                        .content { padding: 25px; }
                        table { width: 100%%; border-collapse: collapse; }
                        th { background: rgba(14,165,233,0.15); color: #0ea5e9; padding: 12px; text-align: left; font-size: 12px; text-transform: uppercase; }
                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🏛️ All Universities Report</h1>
                            <span style="display: inline-block; background: rgba(255,255,255,0.2); padding: 6px 16px; border-radius: 20px; font-size: 12px; margin-top: 12px;">%s Summary</span>
                        </div>
                        <div class="summary">
                            <div class="summary-box"><div class="summary-value" style="color: #0ea5e9">%d</div><div class="summary-label">Universities</div></div>
                            <div class="summary-box"><div class="summary-value">%d</div><div class="summary-label">Total Students</div></div>
                            <div class="summary-box"><div class="summary-value" style="color: #22c55e">%d</div><div class="summary-label">Letters Uploaded</div></div>
                            <div class="summary-box"><div class="summary-value" style="color: #60a5fa">%.1f%%</div><div class="summary-label">Avg Efficiency</div></div>
                        </div>
                        <div class="content">
                            <table>
                                <thead><tr>
                                    <th>University</th>
                                    <th>Coordinator</th>
                                    <th style="text-align: center;">Students</th>
                                    <th style="text-align: center;">Uploaded</th>
                                    <th style="text-align: center;">Efficiency</th>
                                </tr></thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                        <div class="footer">Generated on %s | CampusConnect</div>
                    </div>
                </body>
                </html>
                """
                .formatted(periodLabel, reports.size(), totalStudents, totalLetters, avgEfficiency,
                        rows.toString(), java.time.LocalDate.now().toString());
    }
}
