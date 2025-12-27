package com.cc.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.TelexMan;

import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.StudentRepository;

@Service
@SuppressWarnings("null")
public class TelexService {

        private final StudentRepository studentRepository;
        private final TelexManService telexManService;

        @Autowired
        private S3StorageService s3StorageService;

        @Autowired
        public TelexService(StudentRepository studentRepository, TelexManService telexManService) {
                this.studentRepository = studentRepository;
                this.telexManService = telexManService;
        }

        // 1. Assign Telex Manager
        public Student assignTelexManager(Long studentId, String managerUsername, String assignedBy) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                TelexMan manager = telexManService.getByUsername(managerUsername)
                                .orElseThrow(() -> new RuntimeException("Telex Manager not found"));

                student.setTelexMan(manager);
                student.setApplicationStatus(StudentStatus.TELEX_INITIATED);

                student.setAction(action.UPLOAD_TELEX_DOCS);
                student.setActionOnUser(actionOnUser.telex_manager);

                addHistory(student, StudentStatus.TELEX_INITIATED, assignedBy,
                                "Assigned to Telex Manager: " + managerUsername);

                return studentRepository.save(student);
        }

        // 2. Submit Telex Application to University
        // 2. Submit to University
        public Student submitTelexToUniversity(Long studentId, String telexManagerUsername) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getTelexMan() == null
                                || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
                        throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
                }

                student.setApplicationStatus(StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY);

                student.setAction(action.SUBMIT_TELEX_TO_MEA);
                student.setActionOnUser(actionOnUser.telex_manager);

                addHistory(student, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, telexManagerUsername);
                return studentRepository.save(student);
        }

        // 3. Confirm Submission to MEA
        public Student confirmSubmissionToMEA(Long studentId, String telexManagerUsername) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getTelexMan() == null
                                || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
                        throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
                }

                student.setApplicationStatus(StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA);
                student.setAction(action.NEED_TO_ISSUE_TELEX);
                student.setActionOnUser(actionOnUser.telex_manager);
                addHistory(student, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, telexManagerUsername);
                return studentRepository.save(student);
        }

        // 4. Issue Telex Number
        public Student issueTelexNumber(Long studentId, String telexNumber, String telexManagerUsername) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getTelexMan() == null
                                || !student.getTelexMan().getUsername().equals(telexManagerUsername)) {
                        throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
                }

                student.setTelexNumber(telexNumber);
                student.setTelexIssueDate(java.time.LocalDate.now());
                student.setApplicationStatus(StudentStatus.TELEX_NUMBER_ISSUED);

                student.setAction(action.ASSIGN_TO_VISA_MAN);
                student.setActionOnUser(actionOnUser.admission_manager);

                addHistory(student, StudentStatus.TELEX_NUMBER_ISSUED, telexManagerUsername);

                return studentRepository.save(student);
        }

        // 5. Upload Telex Documents
        public Student uploadTelexDocuments(Long studentId, List<MultipartFile> files, String managerUsername) {
                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                if (student.getTelexMan() == null || !student.getTelexMan().getUsername().equals(managerUsername)) {
                        throw new RuntimeException("Unauthorized: Only assigned Telex Manager can perform this action");
                }

                StringBuilder paths = new StringBuilder();
                if (student.getTelexDocumentsPath() != null) {
                        paths.append(student.getTelexDocumentsPath());
                }

                for (MultipartFile file : files) {
                        String safeName = student.getFullName().replaceAll("\\s+", "_") + "_telex_doc";
                        String path = saveUploadedFile(file, "telex-documents", safeName);
                        if (path != null) {
                                if (paths.length() > 0) {
                                        paths.append(",");
                                }
                                paths.append(path);
                        }
                }

                student.setAction(action.SUBMIT_TELEX_TO_UNIVERSITY);
                student.setActionOnUser(actionOnUser.telex_manager);

                student.setTelexDocumentsPath(paths.toString());
                addHistory(student, student.getApplicationStatus(), managerUsername, "Uploaded Documents");

                return studentRepository.save(student);
        }

        // Helper to save file using S3StorageService
        private String saveUploadedFile(MultipartFile file, String folderName, String filePrefix) {
                if (file == null || file.isEmpty()) {
                        return null;
                }
                return s3StorageService.uploadFile(file, folderName, filePrefix);
        }

        // Helper to add history
        private void addHistory(Student student, StudentStatus status, String updatedBy) {
                addHistory(student, status, updatedBy, null);
        }

        private void addHistory(Student student, StudentStatus status, String updatedBy, String remarks) {
                StudentStatusHistory history = new StudentStatusHistory();
                history.setStudent(student);
                history.setStudentApplicationStatus(status);
                history.setUpdatedBy(updatedBy);
                history.setUpdatedAt(LocalDateTime.now());
                if (remarks != null) {
                        history.setRemarks(remarks);
                }
                student.getStudentStatusHistory().add(history);
        }
        // ================== REPORTING ==================

        @Autowired
        private EmailService emailService;

        public com.cc.DTO.TelexReportDTO getTelexReport(String sectionManagerUsername, java.time.LocalDate date) {
                if (date == null) {
                        date = java.time.LocalDate.now();
                }
                com.cc.DTO.TelexReportDTO report = new com.cc.DTO.TelexReportDTO();

                // Total Students
                report.setTotalStudents(studentRepository.countByTelexManUsername(sectionManagerUsername));

                // Dates
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);

                // Monthly
                LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
                LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

                // Yearly
                LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
                LocalDateTime endOfYear = date.withDayOfYear(date.lengthOfYear()).atTime(23, 59, 59);

                // 0. Assigned to TelexMan (TELEX_INITIATED status in history indicates
                // assignment)
                report.setAssignedToday(studentRepository.countAssignedToTelexManByStatusAndDateRange(
                                sectionManagerUsername, StudentStatus.TELEX_INITIATED, startOfDay, endOfDay));
                report.setAssignedMonth(studentRepository.countAssignedToTelexManByStatusAndDateRange(
                                sectionManagerUsername, StudentStatus.TELEX_INITIATED, startOfMonth, endOfMonth));
                report.setAssignedYear(studentRepository.countAssignedToTelexManByStatusAndDateRange(
                                sectionManagerUsername, StudentStatus.TELEX_INITIATED, startOfYear, endOfYear));

                // 1. Submitted Documents (Using History + specific action/remarks)
                report.setSubmittedDocumentsToday(
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername, startOfDay,
                                                endOfDay));
                report.setSubmittedDocumentsMonth(
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername, startOfMonth,
                                                endOfMonth));
                report.setSubmittedDocumentsYear(
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername, startOfYear,
                                                endOfYear));

                // 2. Submitted to University
                report.setSubmittedToUniversityToday(studentRepository.countByTelexManAndStatusAndDateRange(
                                sectionManagerUsername, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                startOfDay, endOfDay));
                report.setSubmittedToUniversityMonth(
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startOfMonth,
                                                endOfMonth));
                report.setSubmittedToUniversityYear(
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startOfYear,
                                                endOfYear));

                // 3. Submitted to MEA
                report.setSubmittedToMEAToday(studentRepository.countByTelexManAndStatusAndDateRange(
                                sectionManagerUsername,
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfDay, endOfDay));
                report.setSubmittedToMEAMonth(studentRepository.countByTelexManAndStatusAndDateRange(
                                sectionManagerUsername,
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfMonth, endOfMonth));
                report.setSubmittedToMEAYear(studentRepository.countByTelexManAndStatusAndDateRange(
                                sectionManagerUsername,
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfYear, endOfYear));

                // 4. Issued Telex
                report.setTelexIssuedToday(
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfDay, endOfDay));
                report.setTelexIssuedMonth(
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfMonth, endOfMonth));
                report.setTelexIssuedYear(studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                StudentStatus.TELEX_NUMBER_ISSUED, startOfYear, endOfYear));

                // ================== TREND CALCULATIONS ==================

                // Previous Dates
                LocalDateTime startOfYesterday = startOfDay.minusDays(1);
                LocalDateTime endOfYesterday = endOfDay.minusDays(1);

                LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);
                LocalDateTime endOfPrevMonth = endOfMonth.minusMonths(1);
                endOfPrevMonth = startOfPrevMonth.withDayOfMonth(startOfPrevMonth.toLocalDate().lengthOfMonth())
                                .withHour(23)
                                .withMinute(59).withSecond(59);

                // Previous Year
                LocalDateTime startOfPrevYear = startOfYear.minusYears(1);
                LocalDateTime endOfPrevYear = endOfYear.minusYears(1);
                endOfPrevYear = startOfPrevYear.withDayOfYear(startOfPrevYear.toLocalDate().lengthOfYear()).withHour(23)
                                .withMinute(59).withSecond(59);

                // Calculate Assigned Trends
                report.setAssignedTodayTrend(calculateTrend(
                                report.getAssignedToday(),
                                studentRepository.countAssignedToTelexManByStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_INITIATED, startOfYesterday, endOfYesterday)));

                report.setAssignedMonthTrend(calculateTrend(
                                report.getAssignedMonth(),
                                studentRepository.countAssignedToTelexManByStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_INITIATED, startOfPrevMonth, endOfPrevMonth)));

                report.setAssignedYearTrend(calculateTrend(
                                report.getAssignedYear(),
                                studentRepository.countAssignedToTelexManByStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_INITIATED, startOfPrevYear, endOfPrevYear)));

                // Calculate Document Trends
                report.setSubmittedDocumentsTodayTrend(calculateTrend(
                                report.getSubmittedDocumentsToday(),
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername,
                                                startOfYesterday,
                                                endOfYesterday)));

                report.setSubmittedDocumentsMonthTrend(calculateTrend(
                                report.getSubmittedDocumentsMonth(),
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername,
                                                startOfPrevMonth,
                                                endOfPrevMonth)));

                report.setSubmittedDocumentsYearTrend(calculateTrend(
                                report.getSubmittedDocumentsYear(),
                                studentRepository.countTelexDocsUploadedByManager(sectionManagerUsername,
                                                startOfPrevYear,
                                                endOfPrevYear)));

                report.setSubmittedToUniversityTodayTrend(calculateTrend(
                                report.getSubmittedToUniversityToday(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfYesterday, endOfYesterday)));

                report.setSubmittedToUniversityMonthTrend(calculateTrend(
                                report.getSubmittedToUniversityMonth(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfPrevMonth, endOfPrevMonth)));

                report.setSubmittedToUniversityYearTrend(calculateTrend(
                                report.getSubmittedToUniversityYear(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfPrevYear, endOfPrevYear)));

                report.setSubmittedToMEATodayTrend(calculateTrend(
                                report.getSubmittedToMEAToday(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfYesterday,
                                                endOfYesterday)));

                report.setSubmittedToMEAMonthTrend(calculateTrend(
                                report.getSubmittedToMEAMonth(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfPrevMonth,
                                                endOfPrevMonth)));

                report.setSubmittedToMEAYearTrend(calculateTrend(
                                report.getSubmittedToMEAYear(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfPrevYear,
                                                endOfPrevYear)));

                report.setTelexIssuedTodayTrend(calculateTrend(
                                report.getTelexIssuedToday(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfYesterday, endOfYesterday)));

                report.setTelexIssuedMonthTrend(calculateTrend(
                                report.getTelexIssuedMonth(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfPrevMonth, endOfPrevMonth)));

                report.setTelexIssuedYearTrend(calculateTrend(
                                report.getTelexIssuedYear(),
                                studentRepository.countByTelexManAndStatusAndDateRange(sectionManagerUsername,
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfPrevYear, endOfPrevYear)));

                // ================== ACTIVITY PANEL ==================
                List<com.cc.DTO.DailyActivityDTO> activityLogs = activityLogRepository
                                .findDailyActivityCountByUsernameAndDateRange(
                                                sectionManagerUsername, startOfMonth, endOfMonth);
                report.setActivityLogs(activityLogs);

                return report;
        }

        /**
         * Get global/aggregated telex stats for ALL TelexMan users (Admin view)
         */
        public com.cc.DTO.TelexReportDTO getGlobalTelexReport(java.time.LocalDate date) {
                if (date == null) {
                        date = java.time.LocalDate.now();
                }
                com.cc.DTO.TelexReportDTO report = new com.cc.DTO.TelexReportDTO();

                // Total Students assigned to ANY TelexMan
                report.setTotalStudents(studentRepository.countAllTelexManStudents());

                // Dates
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);

                // Monthly
                LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
                LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

                // Yearly
                LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
                LocalDateTime endOfYear = date.withDayOfYear(date.lengthOfYear()).atTime(23, 59, 59);

                // 0. Assigned to TelexMan (TELEX_INITIATED status)
                report.setAssignedToday(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_INITIATED, startOfDay, endOfDay));
                report.setAssignedMonth(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_INITIATED, startOfMonth, endOfMonth));
                report.setAssignedYear(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_INITIATED, startOfYear, endOfYear));

                // 1. Submitted Documents
                report.setSubmittedDocumentsToday(studentRepository.countAllTelexDocsUploaded(startOfDay, endOfDay));
                report.setSubmittedDocumentsMonth(
                                studentRepository.countAllTelexDocsUploaded(startOfMonth, endOfMonth));
                report.setSubmittedDocumentsYear(studentRepository.countAllTelexDocsUploaded(startOfYear, endOfYear));

                // 2. Submitted to University
                report.setSubmittedToUniversityToday(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startOfDay, endOfDay));
                report.setSubmittedToUniversityMonth(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startOfMonth, endOfMonth));
                report.setSubmittedToUniversityYear(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startOfYear, endOfYear));

                // 3. Submitted to MEA
                report.setSubmittedToMEAToday(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfDay, endOfDay));
                report.setSubmittedToMEAMonth(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfMonth, endOfMonth));
                report.setSubmittedToMEAYear(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfYear, endOfYear));

                // 4. Issued Telex
                report.setTelexIssuedToday(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_NUMBER_ISSUED, startOfDay, endOfDay));
                report.setTelexIssuedMonth(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_NUMBER_ISSUED, startOfMonth, endOfMonth));
                report.setTelexIssuedYear(studentRepository.countAllTelexStatusByDateRange(
                                StudentStatus.TELEX_NUMBER_ISSUED, startOfYear, endOfYear));

                // ================== TREND CALCULATIONS ==================
                LocalDateTime startOfYesterday = startOfDay.minusDays(1);
                LocalDateTime endOfYesterday = endOfDay.minusDays(1);

                LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);
                LocalDateTime endOfPrevMonth = startOfPrevMonth
                                .withDayOfMonth(startOfPrevMonth.toLocalDate().lengthOfMonth())
                                .withHour(23).withMinute(59).withSecond(59);

                LocalDateTime startOfPrevYear = startOfYear.minusYears(1);
                LocalDateTime endOfPrevYear = startOfPrevYear
                                .withDayOfYear(startOfPrevYear.toLocalDate().lengthOfYear())
                                .withHour(23).withMinute(59).withSecond(59);

                // Assigned Trends
                report.setAssignedTodayTrend(calculateTrend(report.getAssignedToday(),
                                studentRepository.countAllTelexStatusByDateRange(StudentStatus.TELEX_INITIATED,
                                                startOfYesterday,
                                                endOfYesterday)));
                report.setAssignedMonthTrend(calculateTrend(report.getAssignedMonth(),
                                studentRepository.countAllTelexStatusByDateRange(StudentStatus.TELEX_INITIATED,
                                                startOfPrevMonth,
                                                endOfPrevMonth)));
                report.setAssignedYearTrend(calculateTrend(report.getAssignedYear(),
                                studentRepository.countAllTelexStatusByDateRange(StudentStatus.TELEX_INITIATED,
                                                startOfPrevYear,
                                                endOfPrevYear)));

                // Document Trends
                report.setSubmittedDocumentsTodayTrend(calculateTrend(report.getSubmittedDocumentsToday(),
                                studentRepository.countAllTelexDocsUploaded(startOfYesterday, endOfYesterday)));
                report.setSubmittedDocumentsMonthTrend(calculateTrend(report.getSubmittedDocumentsMonth(),
                                studentRepository.countAllTelexDocsUploaded(startOfPrevMonth, endOfPrevMonth)));
                report.setSubmittedDocumentsYearTrend(calculateTrend(report.getSubmittedDocumentsYear(),
                                studentRepository.countAllTelexDocsUploaded(startOfPrevYear, endOfPrevYear)));

                // University Trends
                report.setSubmittedToUniversityTodayTrend(calculateTrend(report.getSubmittedToUniversityToday(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfYesterday, endOfYesterday)));
                report.setSubmittedToUniversityMonthTrend(calculateTrend(report.getSubmittedToUniversityMonth(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfPrevMonth, endOfPrevMonth)));
                report.setSubmittedToUniversityYearTrend(calculateTrend(report.getSubmittedToUniversityYear(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                                startOfPrevYear, endOfPrevYear)));

                // MEA Trends
                report.setSubmittedToMEATodayTrend(calculateTrend(report.getSubmittedToMEAToday(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfYesterday,
                                                endOfYesterday)));
                report.setSubmittedToMEAMonthTrend(calculateTrend(report.getSubmittedToMEAMonth(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfPrevMonth,
                                                endOfPrevMonth)));
                report.setSubmittedToMEAYearTrend(calculateTrend(report.getSubmittedToMEAYear(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startOfPrevYear,
                                                endOfPrevYear)));

                // Telex Issued Trends
                report.setTelexIssuedTodayTrend(calculateTrend(report.getTelexIssuedToday(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfYesterday, endOfYesterday)));
                report.setTelexIssuedMonthTrend(calculateTrend(report.getTelexIssuedMonth(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfPrevMonth, endOfPrevMonth)));
                report.setTelexIssuedYearTrend(calculateTrend(report.getTelexIssuedYear(),
                                studentRepository.countAllTelexStatusByDateRange(
                                                StudentStatus.TELEX_NUMBER_ISSUED, startOfPrevYear, endOfPrevYear)));

                // Activity Logs - get combined activity for ALL users (using null/empty
                // triggers global query)
                report.setActivityLogs(new java.util.ArrayList<>());

                return report;
        }

        private Double calculateTrend(long current, long previous) {
                if (previous == 0) {
                        return current > 0 ? 100.0 : 0.0;
                }
                return ((double) (current - previous) / previous) * 100.0;
        }

        @Autowired
        private com.cc.repository.ActivityLogRepository activityLogRepository;

        public void sendTelexReportEmail(String managerUsername, java.time.LocalDate date, String recipientEmail,
                        String period) {
                if (date == null)
                        date = java.time.LocalDate.now();

                if (period == null || period.isEmpty()) {
                        period = "Daily"; // Default
                }

                // Use global report for admin/all/empty, individual report for specific manager
                com.cc.DTO.TelexReportDTO report;
                String displayName;

                if (managerUsername == null || managerUsername.isEmpty() ||
                                managerUsername.equalsIgnoreCase("all") || managerUsername.equalsIgnoreCase("admin")) {
                        report = getGlobalTelexReport(date);
                        displayName = "All Telex Managers (Global)";
                } else {
                        report = getTelexReport(managerUsername, date);
                        displayName = managerUsername;
                }

                String subject = period + " Telex Report - " + date + " - " + displayName;
                String htmlContent = generateReportHtml(report, displayName, date, period);

                emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);
        }

        private String generateReportHtml(com.cc.DTO.TelexReportDTO report, String managerName,
                        java.time.LocalDate date,
                        String period) {
                StringBuilder html = new StringBuilder();

                html.append("<html><body style='font-family: Arial, sans-serif;'>");
                html.append(
                                "<div style='max-width: 800px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;'>");

                // Header
                html.append("<div style='background-color: #00796b; color: white; padding: 20px; text-align: center;'>");
                html.append("<h2 style='margin: 0;'>" + period + " Telex Report</h2>");
                html.append("<p style='margin: 5px 0 0;'>" + managerName + " | " + date + "</p>");
                html.append("</div>");

                // Summary Cards
                html.append("<div style='padding: 20px;'>");
                html.append("<h3 style='border-bottom: 2px solid #00796b; padding-bottom: 10px;'>Performance Summary ("
                                + period
                                + " Focus)</h3>");

                html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
                html.append("<thead>");
                html.append("<tr style='background-color: #f2f2f2;'>");
                html.append(
                                "<th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Metric</th>");
                html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Today</th>");
                html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Month</th>");
                html.append("<th style='padding: 10px; border: 1px solid #ddd; text-align: left;'>Year</th>");
                html.append("</tr>");
                html.append("</thead>");
                html.append("<tbody>");

                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Assigned Students</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>"
                                + report.getAssignedToday()
                                + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + report.getAssignedMonth()
                                + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + report.getAssignedYear()
                                + "</td>");
                html.append("</tr>");

                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Submitted Documents</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>"
                                + report.getSubmittedDocumentsToday() + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + report.getSubmittedDocumentsMonth()
                                + "</td>");
                html.append(
                                "<td style='padding: 10px; border: 1px solid #ddd;'>"
                                                + report.getSubmittedDocumentsYear() + "</td>");
                html.append("</tr>");

                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Submitted to University</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>"
                                + report.getSubmittedToUniversityToday() + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>"
                                + report.getSubmittedToUniversityMonth()
                                + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>"
                                + report.getSubmittedToUniversityYear()
                                + "</td>");
                html.append("</tr>");

                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Submitted to MEA</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>"
                                + report.getSubmittedToMEAToday() + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + report.getSubmittedToMEAMonth()
                                + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>" + report.getSubmittedToMEAYear()
                                + "</td>");
                html.append("</tr>");

                html.append("<tr>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd;'>Telex Issued</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold; color: #00796b;'>"
                                + report.getTelexIssuedToday() + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; color: #00796b;'>"
                                + report.getTelexIssuedMonth()
                                + "</td>");
                html.append("<td style='padding: 10px; border: 1px solid #ddd; color: #00796b;'>"
                                + report.getTelexIssuedYear()
                                + "</td>");
                html.append("</tr>");

                html.append("</tbody>");
                html.append("</table>");
                html.append("</div>");

                // Footer
                html.append(
                                "<div style='background-color: #eee; padding: 10px; text-align: center; font-size: 12px; color: #777;'>");
                html.append("<p>Generated by CampusConnect System</p>");
                html.append("</div>");

                html.append("</div></body></html>");
                return html.toString();
        }

        /**
         * Get all TelexMan users with their individual stats for summary table
         * 
         * @param period - "daily", "monthly", or "yearly" (defaults to monthly)
         */
        public java.util.List<com.cc.DTO.TelexManStatsDTO> getAllTelexManStats(String period) {
                java.util.List<com.cc.DTO.TelexManStatsDTO> result = new java.util.ArrayList<>();

                // Get all TelexMan users
                java.util.List<TelexMan> allTelexMen = telexManService.getAll();

                // Calculate date range based on period
                java.time.LocalDate today = java.time.LocalDate.now();
                LocalDateTime startDate;
                LocalDateTime endDate = today.atTime(23, 59, 59);

                if ("daily".equalsIgnoreCase(period)) {
                        startDate = today.atStartOfDay();
                } else if ("yearly".equalsIgnoreCase(period)) {
                        startDate = today.withDayOfYear(1).atStartOfDay();
                } else {
                        // Default to monthly
                        startDate = today.withDayOfMonth(1).atStartOfDay();
                }

                // Calculate stats for each TelexMan within the date range
                for (TelexMan tm : allTelexMen) {
                        String username = tm.getUsername();

                        // Total students assigned (within the period - based on TELEX_INITIATED status
                        // in history)
                        long totalStudents = studentRepository.countAssignedToTelexManByStatusAndDateRange(
                                        username, StudentStatus.TELEX_INITIATED, startDate, endDate);

                        // Get counts by status changes within the period using history
                        long docsUploaded = studentRepository.countTelexDocsUploadedByManager(username, startDate,
                                        endDate);

                        long submittedToUniversity = studentRepository.countByTelexManAndStatusAndDateRange(
                                        username, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, startDate,
                                        endDate);

                        long submittedToMEA = studentRepository.countByTelexManAndStatusAndDateRange(
                                        username, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, startDate, endDate);

                        long telexIssued = studentRepository.countByTelexManAndStatusAndDateRange(
                                        username, StudentStatus.TELEX_NUMBER_ISSUED, startDate, endDate);

                        result.add(new com.cc.DTO.TelexManStatsDTO(
                                        tm.getId(),
                                        username,
                                        tm.getFullName(),
                                        tm.getStatus(),
                                        totalStudents,
                                        docsUploaded,
                                        submittedToUniversity,
                                        submittedToMEA,
                                        telexIssued));
                }

                return result;
        }

        private long countStudentsByTelexManAndStatus(String username, java.util.List<StudentStatus> statuses) {
                return studentRepository.countByTelexManUsernameAndStatusIn(username, statuses);
        }
}
