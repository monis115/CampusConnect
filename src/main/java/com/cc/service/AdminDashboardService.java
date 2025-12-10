package com.cc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.DTO.*;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.repository.*;

/**
 * Service for aggregating admin dashboard data across all user roles and
 * modules.
 */
@Service
public class AdminDashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AddmissionManagerRepository admissionManagerRepository;

    @Autowired
    private UniversityPersonRepository universityPersonRepository;

    @Autowired
    private DocPersonRepository docPersonRepository;

    @Autowired
    private AccountantRepository accountantRepository;

    @Autowired
    private TelexManRepository telexManRepository;

    @Autowired
    private VisaManRepository visaManRepository;

    @Autowired
    private StudentStatusHistoryRepository studentStatusHistoryRepository;

    /**
     * Main method to get consolidated dashboard data
     */
    public AdminDashboardResponseDTO getDashboardData(LocalDate dateFrom, LocalDate dateTo,
            String granularity, int page, int size) {
        AdminDashboardResponseDTO response = new AdminDashboardResponseDTO();

        // Get students filtered by date range once to avoid repeated DB calls if
        // possible,
        // or just pass dates down. For consistency with current impl, we'll pass dates
        // down.
        // Ideally we should filter at DB level, but for now we'll filter the list
        List<Student> filteredStudents = getStudentsByDateRange(dateFrom, dateTo);

        // Set all sections using filtered data
        response.setSystemOverview(getSystemOverview(filteredStudents, dateFrom, dateTo));
        response.setRoleSummaries(getRoleSummaries(filteredStudents));
        response.setStudentPipeline(getStudentPipeline(filteredStudents));
        response.setTimeSeries(getTimeSeriesData(dateFrom, dateTo, granularity)); // Time series does its own date logic
        response.setAlerts(getAlerts(filteredStudents, dateFrom));
        response.setRecentActivities(getRecentActivities(page, size, dateFrom, dateTo));

        // Set metadata
        Map<String, String> filters = new HashMap<>();
        filters.put("dateFrom", dateFrom.toString());
        filters.put("dateTo", dateTo.toString());
        filters.put("granularity", granularity);

        AdminDashboardResponseDTO.DashboardMetadataDTO metadata = new AdminDashboardResponseDTO.DashboardMetadataDTO(
                LocalDateTime.now(), filters);
        response.setMetadata(metadata);

        return response;
    }

    private List<Student> getStudentsByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        // Fetch all and filter, or use a custom query.
        // For accurate dashboarding matching the user's "I can't see changes" request,
        // we must filter the base dataset.
        // We'll treat dateFrom/dateTo as "Created Between" for the main dataset,
        // or we could assume it means "Active Between".
        // Standard admin dashboard behavior: Global filters usually filter by Creation
        // Date.
        List<Student> all = studentRepository.findAll();
        return all.stream()
                .filter(s -> {
                    LocalDate created = s.getCreatedAt();
                    if (created == null)
                        return false;
                    return !created.isBefore(dateFrom) && !created.isAfter(dateTo);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get system-wide overview statistics (Overloaded for Controller Direct Call)
     */
    public SystemOverviewDTO getSystemOverview(LocalDate dateFrom, LocalDate dateTo) {
        return getSystemOverview(getStudentsByDateRange(dateFrom, dateTo), dateFrom, dateTo);
    }

    /**
     * Get system-wide overview statistics
     */
    public SystemOverviewDTO getSystemOverview(List<Student> students, LocalDate dateFrom, LocalDate dateTo) {
        SystemOverviewDTO overview = new SystemOverviewDTO();

        // Counts based on the FILTERED dataset
        overview.setTotalStudents((long) students.size());

        // Agents/Managers etc are system users, their creation date might not be on
        // Student entity.
        // For these users, we should ideally also filter by their creation date if
        // available,
        // but often admin dashboards show "Total System Users" as global.
        // However, to show "interactivity", let's keep user counts global (all time)
        // BUT make student-related counts relative to the date range.
        overview.setTotalAgents(agentRepository.count());
        overview.setTotalAdmissionManagers(admissionManagerRepository.count());
        overview.setTotalUniversityCoordinators(universityPersonRepository.count());
        overview.setTotalDocPersons(docPersonRepository.count());
        overview.setTotalAccountants(accountantRepository.count());
        overview.setTotalTelexPersons(telexManRepository.count());
        overview.setTotalVisaPersons(visaManRepository.count());

        // Activity metrics within the selected range
        // Active students in range (same as total in this filtered context)
        overview.setActiveStudentsToday((long) students.size());

        // New registrations in this range (same as total)
        overview.setNewRegistrationsThisWeek((long) students.size());

        // Visa stamped in this range
        overview.setVisaStampedThisMonth(students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_STAMPED)
                .count());

        return overview;
    }

    /**
     * Get role-specific summaries (Overloaded for Controller Direct Call)
     */
    public RoleSummariesDTO getRoleSummaries(LocalDate dateFrom, LocalDate dateTo) {
        return getRoleSummaries(getStudentsByDateRange(dateFrom, dateTo));
    }

    /**
     * Get role-specific summaries
     */
    public RoleSummariesDTO getRoleSummaries(List<Student> students) {
        RoleSummariesDTO summaries = new RoleSummariesDTO();

        // Usage of 'students' list which is already filtered by date

        // Agent Summary
        long totalStudents = students.size();
        long pendingApplications = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REGISTERED
                        || s.getApplicationStatus() == StudentStatus.UNDER_REVIEW
                        || s.getApplicationStatus() == StudentStatus.Submitted)
                .count();
        long visaApproved = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_STAMPED)
                .count();
        long rejected = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REJECTED)
                .count();
        summaries.setAgent(
                new RoleSummariesDTO.AgentSummary(totalStudents, pendingApplications, visaApproved, rejected));

        // Admission Manager Summary
        long newApplications = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REGISTERED
                        || s.getApplicationStatus() == StudentStatus.Submitted)
                .count();
        long pendingUniversityAction = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.UNDER_REVIEW
                        || s.getApplicationStatus() == StudentStatus.FORWARDED_TO_UNI)
                .count();
        long admissionLettersGenerated = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.ADMISSION_LETTER_GENERATED
                        || s.getApplicationStatus() == StudentStatus.ADMISSION_LETTER_APPROVED)
                .count();
        summaries.setAdmissionManager(new RoleSummariesDTO.AdmissionManagerSummary(
                newApplications, pendingUniversityAction, admissionLettersGenerated));

        // University Coordinator Summary
        long pendingAdmissions = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.PENDING_ADMISSION_LETTER
                        || s.getApplicationStatus() == StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED)
                .count();
        long admittedStudents = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.ADMISSION_LETTER_GENERATED
                        || s.getApplicationStatus() == StudentStatus.ADMISSION_LETTER_APPROVED
                        || s.getApplicationStatus() == StudentStatus.COMPLETED)
                .count();
        summaries.setUniversityCoordinator(new RoleSummariesDTO.UniversityCoordinatorSummary(
                pendingAdmissions, admittedStudents));

        // Doc Person Summary
        long pendingDocVerification = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.TELEX_INITIATED
                        && (s.getIsTelexDocApproved() == null || !s.getIsTelexDocApproved()))
                .count();
        long docsReceived = students.stream()
                .filter(s -> s.getDocReceivedDate() != null)
                .count();
        long docsReturned = students.stream()
                .filter(s -> s.getDocReturnedDate() != null)
                .count();
        summaries.setDocPerson(new RoleSummariesDTO.DocPersonSummary(
                pendingDocVerification, docsReceived, docsReturned));

        // Accountant Summary
        long pendingPayments = students.stream()
                .filter(s -> "Pending".equals(s.getPaymentStatus()))
                .count();
        long paymentsReceived = students.stream()
                .filter(s -> "Received".equals(s.getPaymentStatus()))
                .count();
        double totalAmountCollected = students.stream()
                .filter(s -> s.getTelexPaymentReceivedAmount() != null)
                .mapToDouble(Student::getTelexPaymentReceivedAmount)
                .sum();
        summaries.setAccountant(new RoleSummariesDTO.AccountantSummary(
                pendingPayments, paymentsReceived, totalAmountCollected));

        // Telex Person Summary
        long pendingTelex = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.APPLIED_FOR_TELEX
                        || s.getApplicationStatus() == StudentStatus.TELEX_FEE_RECIEVED
                        || s.getApplicationStatus() == StudentStatus.TELEX_INITIATED)
                .count();
        long submittedToUniversity = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY
                        || s.getApplicationStatus() == StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA
                        || s.getApplicationStatus() == StudentStatus.TELEX_NUMBER_ISSUED)
                .count();
        long submittedToMEA = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA
                        || s.getApplicationStatus() == StudentStatus.TELEX_NUMBER_ISSUED)
                .count();
        long telexIssued = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.TELEX_NUMBER_ISSUED
                        || s.getTelexNumber() != null)
                .count();
        summaries.setTelexPerson(new RoleSummariesDTO.TelexPersonSummary(
                pendingTelex, submittedToUniversity, submittedToMEA, telexIssued));

        // Visa Person Summary
        long pendingVisaFee = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_FEE_PENDING)
                .count();
        long passportSubmitted = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY)
                .count();
        long visaApplied = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_APPLIED
                        || s.getApplicationStatus() == StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY)
                .count();
        long visaStamped = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_STAMPED)
                .count();
        summaries.setVisaPerson(new RoleSummariesDTO.VisaPersonSummary(
                pendingVisaFee, passportSubmitted, visaApplied, visaStamped));

        return summaries;
    }

    /**
     * Get student pipeline (Overloaded for Controller Direct Call)
     */
    public Map<String, Long> getStudentPipeline(LocalDate dateFrom, LocalDate dateTo) {
        return getStudentPipeline(getStudentsByDateRange(dateFrom, dateTo));
    }

    /**
     * Get student pipeline - count by each status
     */
    public Map<String, Long> getStudentPipeline(List<Student> students) {
        Map<String, Long> pipeline = new LinkedHashMap<>();

        // Get counts for each status from filtered list
        for (StudentStatus status : StudentStatus.values()) {
            long count = students.stream()
                    .filter(s -> s.getApplicationStatus() == status)
                    .count();
            if (count > 0) {
                pipeline.put(status.name(), count);
            }
        }

        return pipeline;
    }

    /**
     * Get time series data based on granularity
     */
    public TimeSeriesDTO getTimeSeriesData(LocalDate dateFrom, LocalDate dateTo, String granularity) {
        TimeSeriesDTO timeSeries = new TimeSeriesDTO();
        timeSeries.setGranularity(granularity);
        timeSeries.setDateFrom(dateFrom);
        timeSeries.setDateTo(dateTo);

        List<TimeSeriesDTO.DataPoint> dataPoints = new ArrayList<>();
        List<Student> allStudents = studentRepository.findAll();

        // 1. Prepare maps to hold counts for each metric by date
        Map<LocalDate, Long> admissionMap = new HashMap<>();
        Map<LocalDate, Long> visaMap = new HashMap<>();
        Map<LocalDate, Long> telexMap = new HashMap<>();

        // 2. Populate maps from student history
        for (Student student : allStudents) {
            List<StudentStatusHistory> history = student.getStudentStatusHistory();
            if (history != null) {
                for (StudentStatusHistory h : history) {
                    if (h.getUpdatedAt() != null && h.getNewStatus() != null) {
                        LocalDate date = h.getUpdatedAt().toLocalDate();

                        // Count Admission Letters Generated
                        if (h.getNewStatus() == StudentStatus.ADMISSION_LETTER_GENERATED) {
                            admissionMap.put(date, admissionMap.getOrDefault(date, 0L) + 1);
                        }

                        // Count Visa Stamped
                        if (h.getNewStatus() == StudentStatus.VISA_STAMPED) {
                            visaMap.put(date, visaMap.getOrDefault(date, 0L) + 1);
                        }

                        // Count Telex Issued
                        if (h.getNewStatus() == StudentStatus.TELEX_NUMBER_ISSUED
                                || h.getNewStatus() == StudentStatus.TELEX_INITIATED) {
                            telexMap.put(date, telexMap.getOrDefault(date, 0L) + 1);
                        }
                    }
                }
            }
        }

        // Group by date based on granularity
        if ("daily".equalsIgnoreCase(granularity)) {
            LocalDate current = dateFrom;
            while (!current.isAfter(dateTo)) {
                final LocalDate date = current;

                // New Registrations (Using CreatedAt)
                long newRegs = allStudents.stream()
                        .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().equals(date))
                        .count();

                // Other metrics from maps
                long admissions = admissionMap.getOrDefault(date, 0L);
                long visas = visaMap.getOrDefault(date, 0L);
                long telex = telexMap.getOrDefault(date, 0L);

                dataPoints.add(new TimeSeriesDTO.DataPoint(date, newRegs, visas, admissions, telex));
                current = current.plusDays(1);
            }
        } else if ("weekly".equalsIgnoreCase(granularity)) {
            LocalDate current = dateFrom;
            while (!current.isAfter(dateTo)) {
                final LocalDate weekStart = current;
                final LocalDate weekEnd = current.plusDays(6);

                // New Registrations
                long newRegs = allStudents.stream()
                        .filter(s -> s.getCreatedAt() != null
                                && !s.getCreatedAt().isBefore(weekStart)
                                && !s.getCreatedAt().isAfter(weekEnd))
                        .count();

                // Sum metrics for the week range
                long admissions = 0;
                long visas = 0;
                long telex = 0;

                LocalDate tempDate = weekStart;
                while (!tempDate.isAfter(weekEnd)) {
                    admissions += admissionMap.getOrDefault(tempDate, 0L);
                    visas += visaMap.getOrDefault(tempDate, 0L);
                    telex += telexMap.getOrDefault(tempDate, 0L);
                    tempDate = tempDate.plusDays(1);
                }

                dataPoints.add(new TimeSeriesDTO.DataPoint(weekStart, newRegs, visas, admissions, telex));
                current = current.plusWeeks(1);
            }
        } else { // monthly
            LocalDate current = dateFrom.with(TemporalAdjusters.firstDayOfMonth());
            while (!current.isAfter(dateTo)) {
                final LocalDate monthStart = current;
                final LocalDate monthEnd = current.with(TemporalAdjusters.lastDayOfMonth());

                long newRegs = allStudents.stream()
                        .filter(s -> s.getCreatedAt() != null
                                && !s.getCreatedAt().isBefore(monthStart)
                                && !s.getCreatedAt().isAfter(monthEnd))
                        .count();

                long admissions = 0;
                long visas = 0;
                long telex = 0;

                LocalDate tempDate = monthStart;
                while (!tempDate.isAfter(monthEnd)) {
                    admissions += admissionMap.getOrDefault(tempDate, 0L);
                    visas += visaMap.getOrDefault(tempDate, 0L);
                    telex += telexMap.getOrDefault(tempDate, 0L);
                    tempDate = tempDate.plusDays(1);
                }

                dataPoints.add(new TimeSeriesDTO.DataPoint(monthStart, newRegs, visas, admissions, telex));
                current = current.plusMonths(1);
            }
        }

        timeSeries.setData(dataPoints);
        return timeSeries;
    }

    /**
     * Get system alerts (Overloaded for Controller Direct Call)
     */
    public List<AlertDTO> getAlerts(LocalDate dateFrom) {
        // For alerts, if dateTo isn't specified in the context, we usually assume "now"
        // or just use the from date to filter context.
        // The main dashboard passes dateFrom and dateTo (defaulting to now).
        // Let's assume dateTo is now for this context.
        return getAlerts(getStudentsByDateRange(dateFrom, LocalDate.now()), dateFrom);
    }

    /**
     * Get system alerts
     */
    public List<AlertDTO> getAlerts(List<Student> students, LocalDate dateFrom) {
        List<AlertDTO> alerts = new ArrayList<>();
        // Alerts usually depend on "Current State" regardless of creation date,
        // but if we are in "Review Mode" for a past period, maybe we only want alerts
        // for those students.
        // We'll use the filtered list.
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        // Alert: Students stuck in REGISTERED status
        long stuckInRegistered = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REGISTERED
                        && s.getCreatedAt() != null
                        && s.getCreatedAt().isBefore(sevenDaysAgo))
                .count();
        if (stuckInRegistered > 0) {
            alerts.add(new AlertDTO("STUCK_IN_STATUS", "WARNING",
                    stuckInRegistered + " students pending review for >7 days", stuckInRegistered));
        }

        // Alert: Pending payments
        long pendingPayments = students.stream()
                .filter(s -> "Pending".equals(s.getPaymentStatus())
                        && s.getCreatedAt() != null
                        && s.getCreatedAt().isBefore(sevenDaysAgo))
                .count();
        if (pendingPayments > 0) {
            alerts.add(new AlertDTO("PAYMENT_PENDING", "WARNING",
                    pendingPayments + " students have pending payments for >7 days", pendingPayments));
        }

        // Alert: Documents pending verification
        long docsPending = students.stream()
                .filter(s -> s.getDocReceivedDate() != null
                        && (s.getIsTelexDocApproved() == null || !s.getIsTelexDocApproved()))
                .count();
        if (docsPending > 0) {
            alerts.add(new AlertDTO("DOCS_PENDING_VERIFICATION", "INFO",
                    docsPending + " students have documents awaiting verification", docsPending));
        }

        // Alert: Visa applications in progress
        long visaInProgress = students.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.VISA_APPLIED
                        || s.getApplicationStatus() == StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY)
                .count();
        if (visaInProgress > 0) {
            alerts.add(new AlertDTO("VISA_IN_PROGRESS", "INFO",
                    visaInProgress + " visa applications in progress", visaInProgress));
        }

        return alerts;
    }

    /**
     * Get recent activities from status history
     */
    public List<RecentActivityDTO> getRecentActivities(int page, int size, LocalDate dateFrom, LocalDate dateTo) {
        // Since we want activities within the date range, we should leverage the repo
        // methods
        // that can filter by updated_at or similar if available, or filter in memory.
        // The previous impl used findTop10ByOrderByIdDesc which ignores dates.

        List<RecentActivityDTO> activities = new ArrayList<>();
        // Use the filtered students list approach?
        // Recent activity is time-based. We should conceptually ignore creation date
        // filter
        // and look for "Events happening between DateFrom and DateTo".
        // But for simplicity and consistency with the "Students created in this range"
        // view,
        // let's stick to showing activity for the *filtered students*.

        // Actually, "Recent Activities" usually implies "Activities occurring in the
        // selected window".
        // Let's implement activity filtering by event date.

        try {
            List<Student> allStudents = studentRepository.findAll();

            List<RecentActivityDTO> allActivities = new ArrayList<>();

            for (Student student : allStudents) {
                List<StudentStatusHistory> history = student.getStudentStatusHistory();

                if (history != null && !history.isEmpty()) {
                    for (StudentStatusHistory h : history) {
                        LocalDateTime eventTime = h.getUpdatedAt() != null ? h.getUpdatedAt() : LocalDateTime.now(); // corrected
                                                                                                                     // method
                                                                                                                     // naming
                                                                                                                     // conventions
                                                                                                                     // if
                                                                                                                     // needed
                        if (eventTime == null)
                            eventTime = LocalDateTime.now();

                        LocalDate eventDate = eventTime.toLocalDate();
                        if (!eventDate.isBefore(dateFrom) && !eventDate.isAfter(dateTo)) {
                            allActivities.add(new RecentActivityDTO(
                                    student.getId(),
                                    student.getApplicationId(),
                                    student.getFullName(),
                                    "STATUS_CHANGE",
                                    null,
                                    h.getNewStatus() != null ? h.getNewStatus().name() : null,
                                    eventTime,
                                    h.getUpdatedBy()));
                        }
                    }
                }
                // Add creation as an activity if within range
                if (student.getCreatedAt() != null) {
                    LocalDate createDate = student.getCreatedAt();
                    if (!createDate.isBefore(dateFrom) && !createDate.isAfter(dateTo)) {
                        allActivities.add(new RecentActivityDTO(
                                student.getId(),
                                student.getApplicationId(),
                                student.getFullName(),
                                "STUDENT_CREATED",
                                null,
                                "REGISTERED",
                                student.getCreatedAt().atStartOfDay(),
                                student.getUpdatedBy()));
                    }
                }
            }

            // Sort by date desc
            allActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            // Pagination
            int start = page * size;
            int end = Math.min(start + size, allActivities.size());
            if (start < allActivities.size()) {
                activities = allActivities.subList(start, end);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return activities;
    }
}
