package com.cc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cc.DTO.*;
import com.cc.DTO.CollegePerformanceDashboardDTO.DashboardMetadata;
import com.cc.DTO.CollegeTimeSeriesDTO.CollegeDataPoint;
import com.cc.DTO.EntityPerformanceDTO.EntityMetrics;
import com.cc.DTO.StudentPipelineDTO.PipelineStage;
import com.cc.modals.*;
import com.cc.repository.*;

/**
 * Service for College Performance Dashboard.
 * Aggregates all metrics and analytics for a specific college.
 */
@Service
public class CollegePerformanceService {

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentStatusHistoryRepository historyRepository;

    // Removed unused repositories to fix warnings
    // private TelexManRepository telexManRepository;
    // private VisaManRepository visaManRepository;

    @Autowired
    private DocPersonRepository docPersonRepository;

    @Autowired
    private AccountantRepository accountantRepository;

    // Status groups for pipeline stages
    private static final List<StudentStatus> REGISTERED_STATUSES = List.of(
            StudentStatus.Submitted, StudentStatus.REGISTERED, StudentStatus.UNDER_REVIEW,
            StudentStatus.PENDING_APPROVAL, StudentStatus.APPROVED);

    private static final List<StudentStatus> ADMISSION_LETTER_STATUSES = List.of(
            StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED, StudentStatus.PENDING_ADMISSION_LETTER,
            StudentStatus.ADMISSION_LETTER_GENERATED, StudentStatus.ADMISSION_LETTER_APPROVED);

    private static final List<StudentStatus> TELEX_STATUSES = List.of(
            StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED, StudentStatus.APPLIED_FOR_TELEX,
            StudentStatus.TELEX_FEE_RECIEVED, StudentStatus.TELEX_INITIATED,
            StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
            StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, StudentStatus.TELEX_NUMBER_ISSUED,
            StudentStatus.DOCS_VARIFICATION_COMPLETE, StudentStatus.DOCS_VARIFICATION_APPROVED);

    private static final List<StudentStatus> VISA_STATUSES = List.of(
            StudentStatus.VISA_FEE_PENDING, StudentStatus.VISA_FEE_RECEIVED,
            StudentStatus.VISA_APPLIED, StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY,
            StudentStatus.VISA_PAYMENT_CONFIRMED, StudentStatus.PASSPORT_COLLECTED);

    private static final List<StudentStatus> COMPLETED_STATUSES = List.of(
            StudentStatus.VISA_STAMPED, StudentStatus.COMPLETED, StudentStatus.TRAVELLING);

    private static final List<StudentStatus> ACTIVE_PIPELINE_STATUSES;
    static {
        List<StudentStatus> active = new ArrayList<>();
        active.addAll(REGISTERED_STATUSES);
        active.addAll(ADMISSION_LETTER_STATUSES);
        active.addAll(TELEX_STATUSES);
        active.addAll(VISA_STATUSES);
        ACTIVE_PIPELINE_STATUSES = Collections.unmodifiableList(active);
    }

    /**
     * Get complete dashboard data for a college.
     */
    public CollegePerformanceDashboardDTO getDashboard(Long collegeId, LocalDate dateFrom, LocalDate dateTo) {
        // Validate and get college
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found with ID: " + collegeId));

        // Set default dates if not provided
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusDays(30);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }

        CollegePerformanceDashboardDTO dashboard = new CollegePerformanceDashboardDTO();

        // Build each section
        dashboard.setCollegeInfo(buildCollegeInfo(college));
        dashboard.setStatsOverview(calculateStatsOverview(college, collegeId, dateFrom, dateTo));
        dashboard.setStudentPipeline(buildStudentPipeline(collegeId, dateFrom, dateTo)); // Pass dates
        dashboard.setEntityPerformance(calculateEntityPerformance(college, collegeId));
        dashboard.setTimeSeries(buildTimeSeries(collegeId, dateFrom, dateTo));
        dashboard.setRecentActivities(getRecentActivities(collegeId, 20));
        dashboard.setAlerts(generateAlerts(collegeId));
        dashboard.setMetadata(new DashboardMetadata(dateFrom, dateTo));

        return dashboard;
    }

    /**
     * Get all colleges for dropdown selection.
     */
    public List<CollegeInfoDTO> getAllCollegesForDropdown() {
        return collegeRepository.findAll().stream()
                .map(this::buildCollegeInfo)
                .collect(Collectors.toList());
    }

    // ======================= PRIVATE HELPER METHODS =======================

    private CollegeInfoDTO buildCollegeInfo(College college) {
        CollegeInfoDTO info = new CollegeInfoDTO();
        info.setId(college.getId());
        info.setCollegeName(college.getCollegeName());
        info.setCity(college.getCity());
        info.setCountry(college.getCountry());

        if (college.getAdmissionManager() != null) {
            info.setAdmissionManagerName(college.getAdmissionManager().getFullName());
        }

        if (college.getUniversityPerson() != null) {
            info.setUniversityCoordinatorName(college.getUniversityPerson().getUserName());
        }

        return info;
    }

    private CollegeStatsOverviewDTO calculateStatsOverview(College college, Long collegeId,
            LocalDate dateFrom, LocalDate dateTo) {
        CollegeStatsOverviewDTO stats = new CollegeStatsOverviewDTO();

        // Total students for this college (REGISTERED IN THIS PERIOD)
        long totalStudents = studentRepository.countByCollege_IdAndCreatedAtBetween(collegeId, dateFrom, dateTo);
        stats.setTotalStudents(totalStudents);

        // Active pipeline (Joined in this period and currently active)
        // Active pipeline (Joined in this period and currently active)

        // We will calculate active pipeline based on status distribution within this
        // COHORT
        List<Student> cohort = studentRepository.findByCollege_IdAndCreatedAtBetween(collegeId, dateFrom, dateTo);
        long activeCount = cohort.stream()
                .filter(s -> ACTIVE_PIPELINE_STATUSES.contains(s.getApplicationStatus()) &&
                        !COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();
        stats.setActivePipeline(activeCount);

        // Visa success rate: (Visa Stamped from THIS COHORT / Total Students in Cohort)
        // * 100
        long visaStampedInCohort = cohort.stream()
                .filter(s -> COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();

        double successRate = totalStudents > 0 ? (visaStampedInCohort * 100.0 / totalStudents) : 0.0;
        stats.setVisaSuccessRate(Math.round(successRate * 10.0) / 10.0);

        // Conversion rate: (Travelling from THIS COHORT / Visa Applied from THIS
        // COHORT) * 100
        long visaAppliedInCohort = cohort.stream()
                .filter(s -> VISA_STATUSES.contains(s.getApplicationStatus())
                        || COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();

        long travellingInCohort = cohort.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.TRAVELLING)
                .count();

        double conversionRate = visaAppliedInCohort > 0 ? (travellingInCohort * 100.0 / visaAppliedInCohort) : 0.0;
        stats.setConversionRate(Math.round(conversionRate * 10.0) / 10.0);

        // New registrations in period
        long newRegistrations = studentRepository.countByCollege_IdAndCreatedAtBetween(collegeId, dateFrom, dateTo);
        stats.setNewRegistrationsInPeriod(newRegistrations);

        // Visa stamped in period (from history)
        LocalDateTime startDateTime = dateFrom.atStartOfDay();
        LocalDateTime endDateTime = dateTo.atTime(23, 59, 59);
        long visaStampedInPeriod = historyRepository.countStatusTransitionsByCollegeAndDateRange(
                collegeId, StudentStatus.VISA_STAMPED, startDateTime, endDateTime);
        stats.setVisaStampedInPeriod(visaStampedInPeriod);

        // Rejected in period
        long rejectedInPeriod = historyRepository.countStatusTransitionsByCollegeAndDateRange(
                collegeId, StudentStatus.REJECTED, startDateTime, endDateTime);
        stats.setRejectedInPeriod(rejectedInPeriod);

        // Completed in period
        long completedInPeriod = historyRepository.countStatusTransitionsByCollegeAndDateRange(
                collegeId, StudentStatus.COMPLETED, startDateTime, endDateTime);
        stats.setCompletedInPeriod(completedInPeriod);

        // Average processing days (simplified calculation)
        stats.setAvgProcessingDays(calculateAvgProcessingDays(collegeId));

        // Calculate trends (BENCHMARKING vs Global Average)
        long totalColleges = collegeRepository.count();
        if (totalColleges == 0)
            totalColleges = 1;

        // 1. Total Students Trend (Benchmarking vs Avg College)
        long globalTotalRegistrations = studentRepository.countByCreatedAtBetween(dateFrom, dateTo);
        double avgRegistrationsPerCollege = (double) globalTotalRegistrations / totalColleges;
        stats.setTotalStudentsTrend(calculateBenchmarkTrend((double) newRegistrations, avgRegistrationsPerCollege));

        // 2. Active Pipeline Trend (Benchmarking vs Avg College)
        // Global count of students in active statuses created in this period
        long globalActiveCount = studentRepository
                .countByApplicationStatusInAndCreatedAtBetween(ACTIVE_PIPELINE_STATUSES, dateFrom, dateTo);
        double avgPipelinePerCollege = (double) globalActiveCount / totalColleges;
        stats.setActivePipelineTrend(calculateBenchmarkTrend((double) activeCount, avgPipelinePerCollege));

        // 3. Visa Success Rate Trend (Benchmarking vs Global Avg Rate)
        // Global Success Rate = Global Stamped / Global Total Students (in period)
        long globalStamped = studentRepository.countByApplicationStatusInAndCreatedAtBetween(
                COMPLETED_STATUSES, dateFrom, dateTo);

        double globalSuccessRate = globalTotalRegistrations > 0 ? (globalStamped * 100.0 / globalTotalRegistrations)
                : 0.0;
        // Difference in percentage points
        stats.setVisaSuccessRateTrend(Math.round((successRate - globalSuccessRate) * 10.0) / 10.0);

        // 4. Conversion Rate Trend (Benchmarking vs Global Avg Rate)
        long globalVisaApplied = studentRepository.countByApplicationStatusInAndCreatedAtBetween(VISA_STATUSES,
                dateFrom, dateTo)
                + studentRepository.countByApplicationStatusInAndCreatedAtBetween(COMPLETED_STATUSES, dateFrom, dateTo);
        long globalTravelling = studentRepository.countByApplicationStatusAndCreatedAtBetween(StudentStatus.TRAVELLING,
                dateFrom, dateTo);

        double globalConversion = globalVisaApplied > 0 ? (globalTravelling * 100.0 / globalVisaApplied) : 0.0;
        // Difference in percentage points
        stats.setConversionRateTrend(Math.round((stats.getConversionRate() - globalConversion) * 10.0) / 10.0);

        // 5. Avg Processing Days Trend (Benchmarking vs Global Avg Speed)
        double globalAvgDays = calculateGlobalAvgProcessingDays(dateFrom, dateTo);
        // Trend = Difference (Global - College). Positive means FASTER.
        stats.setAvgProcessingDaysTrend(Math.round((globalAvgDays - stats.getAvgProcessingDays()) * 10.0) / 10.0);

        return stats;
    }

    private Double calculateGlobalAvgProcessingDays(LocalDate start, LocalDate end) {
        List<Student> globalCompleted = studentRepository
                .findByApplicationStatusInAndCreatedAtBetween(COMPLETED_STATUSES, start, end);
        if (globalCompleted.isEmpty())
            return 0.0;

        long totalDays = 0;
        int count = 0;
        for (Student s : globalCompleted) {
            // Find history for completion (Heavy, but required for global avg)
            List<StudentStatusHistory> history = historyRepository.findByStudentId(s.getId()); // Inefficient loop
            for (StudentStatusHistory h : history) {
                if (COMPLETED_STATUSES.contains(h.getStudentApplicationStatus())) {
                    long days = ChronoUnit.DAYS.between(s.getCreatedAt(), h.getUpdatedAt().toLocalDate());
                    totalDays += days;
                    count++;
                    break;
                }
            }
        }
        return count > 0 ? Math.round((totalDays * 10.0) / count) / 10.0 : 0.0;
    }

    private Double calculateTrend(long current, long previous) {
        if (previous == 0)
            return current > 0 ? 100.0 : 0.0;
        return Math.round(((current - previous) * 100.0 / previous) * 10.0) / 10.0;
    }

    private Double calculateBenchmarkTrend(double current, double average) {
        if (average == 0)
            return current > 0 ? 100.0 : 0.0;
        return Math.round(((current - average) * 100.0 / average) * 10.0) / 10.0;
    }

    private Double calculateAvgProcessingDays(Long collegeId) {
        // Get all completed students and calculate average time from creation to
        // completion
        List<Student> completedStudents = studentRepository.findByCollege_IdAndApplicationStatusIn(
                collegeId, COMPLETED_STATUSES);

        if (completedStudents.isEmpty()) {
            return 0.0;
        }

        long totalDays = 0;
        int count = 0;

        for (Student student : completedStudents) {
            if (student.getCreatedAt() != null) {
                // Find the completion date from history
                List<StudentStatusHistory> history = historyRepository.findByStudentId(student.getId());
                for (StudentStatusHistory h : history) {
                    if (COMPLETED_STATUSES.contains(h.getStudentApplicationStatus())) {
                        long days = ChronoUnit.DAYS.between(student.getCreatedAt(),
                                h.getUpdatedAt().toLocalDate());
                        totalDays += days;
                        count++;
                        break;
                    }
                }
            }
        }

        return count > 0 ? Math.round((totalDays * 10.0) / count) / 10.0 : 0.0;
    }

    private StudentPipelineDTO buildStudentPipeline(Long collegeId, LocalDate dateFrom, LocalDate dateTo) {
        StudentPipelineDTO pipeline = new StudentPipelineDTO();
        List<PipelineStage> stages = new ArrayList<>();

        // Get cohort for this period
        List<Student> cohort = studentRepository.findByCollege_IdAndCreatedAtBetween(collegeId, dateFrom, dateTo);
        long totalStudents = cohort.size();
        pipeline.setTotalStudents(totalStudents);

        // Rejected count within cohort
        long rejectedCount = cohort.stream()
                .filter(s -> s.getApplicationStatus() == StudentStatus.REJECTED)
                .count();
        pipeline.setRejectedCount(rejectedCount);

        // Build pipeline stages based on status hierarchy in THIS COHORT

        // Stage 1: Registered (Everyone in cohort)
        stages.add(buildStage("Registered", "REGISTERED", totalStudents, totalStudents, null));

        // Stage 2: Admission Letter
        // Count cohort members who have reached ADMISSION_LETTER or beyond
        long admissionLetterCount = cohort.stream()
                .filter(s -> ADMISSION_LETTER_STATUSES.contains(s.getApplicationStatus()) ||
                        TELEX_STATUSES.contains(s.getApplicationStatus()) ||
                        VISA_STATUSES.contains(s.getApplicationStatus()) ||
                        COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();
        double dropOff1 = totalStudents > 0 ? ((totalStudents - admissionLetterCount) * 100.0 / totalStudents) : 0;
        stages.add(buildStage("Admission Letter", "ADMISSION_LETTER", admissionLetterCount, totalStudents, dropOff1));

        // Stage 3: Telex Initiated
        long telexCount = cohort.stream()
                .filter(s -> TELEX_STATUSES.contains(s.getApplicationStatus()) ||
                        VISA_STATUSES.contains(s.getApplicationStatus()) ||
                        COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();
        double dropOff2 = admissionLetterCount > 0
                ? ((admissionLetterCount - telexCount) * 100.0 / admissionLetterCount)
                : 0;
        stages.add(buildStage("Telex Initiated", "TELEX", telexCount, totalStudents, dropOff2));

        // Stage 4: Visa Applied
        long visaAppliedCount = cohort.stream()
                .filter(s -> VISA_STATUSES.contains(s.getApplicationStatus()) ||
                        COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();
        double dropOff3 = telexCount > 0 ? ((telexCount - visaAppliedCount) * 100.0 / telexCount) : 0;
        stages.add(buildStage("Visa Applied", "VISA_APPLIED", visaAppliedCount, totalStudents, dropOff3));

        // Stage 5: Visa Stamped / Completed (Including Travelling)
        long completedCount = cohort.stream()
                .filter(s -> COMPLETED_STATUSES.contains(s.getApplicationStatus()))
                .count();
        double dropOff4 = visaAppliedCount > 0 ? ((visaAppliedCount - completedCount) * 100.0 / visaAppliedCount) : 0;
        stages.add(buildStage("Visa Stamped", "VISA_STAMPED", completedCount, totalStudents, dropOff4));

        pipeline.setStages(stages);
        return pipeline;
    }

    private PipelineStage buildStage(String name, String key, Long count, Long total, Double dropOff) {
        PipelineStage stage = new PipelineStage();
        stage.setStageName(name);
        stage.setStageKey(key);
        stage.setCount(count);
        stage.setPercentage(total > 0 ? Math.round(count * 1000.0 / total) / 10.0 : 0.0);
        stage.setDropOffRate(dropOff != null ? Math.round(dropOff * 10.0) / 10.0 : null);
        return stage;
    }

    private EntityPerformanceDTO calculateEntityPerformance(College college, Long collegeId) {
        EntityPerformanceDTO performance = new EntityPerformanceDTO();

        // Admission Manager
        if (college.getAdmissionManager() != null) {
            AddmissionManager am = college.getAdmissionManager();
            EntityMetrics amMetrics = new EntityMetrics(am.getId(), am.getFullName(), "Admission Manager");
            amMetrics.setStatus(am.getStatus());
            amMetrics.setStudentsHandled(studentRepository.countByCollege_Id(collegeId));
            // Count students with admission letters
            long completed = studentRepository.countByCollege_IdAndApplicationStatusIn(collegeId,
                    ADMISSION_LETTER_STATUSES);
            amMetrics.setStudentsCompleted(completed);
            performance.setAdmissionManager(amMetrics);
        }

        // University Coordinator
        if (college.getUniversityPerson() != null) {
            UniversityPerson up = college.getUniversityPerson();
            EntityMetrics upMetrics = new EntityMetrics(up.getId(), up.getUserName(), "University Coordinator");
            upMetrics.setStatus(up.getStatus());
            // Count students in telex and beyond stages
            long inProgress = studentRepository.countByCollege_IdAndApplicationStatusIn(collegeId, TELEX_STATUSES);
            upMetrics.setStudentsInProgress(inProgress);
            upMetrics.setStudentsHandled(studentRepository.countByCollege_Id(collegeId));
            performance.setUniversityCoordinator(upMetrics);
        }

        // TelexMen - find all telex men who have worked with this college's students
        List<TelexMan> telexMen = studentRepository.findDistinctTelexMenByCollegeId(collegeId);
        List<EntityMetrics> telexMetrics = new ArrayList<>();
        for (TelexMan tm : telexMen) {
            EntityMetrics tmMetrics = new EntityMetrics(tm.getId(), tm.getUsername(), "Telex Handler");
            tmMetrics.setStatus(tm.getStatus());
            long count = studentRepository.countByCollegeAndTelexMan(collegeId, tm.getId());
            tmMetrics.setStudentsHandled(count);
            telexMetrics.add(tmMetrics);
        }
        performance.setTelexMen(telexMetrics);

        // VisaMen - find all visa men who have worked with this college's students
        List<VisaMan> visaMen = studentRepository.findDistinctVisaMenByCollegeId(collegeId);
        List<EntityMetrics> visaMetrics = new ArrayList<>();
        for (VisaMan vm : visaMen) {
            EntityMetrics vmMetrics = new EntityMetrics(vm.getId(), vm.getUsername(), "Visa Handler");
            vmMetrics.setStatus(vm.getStatus());
            long count = studentRepository.countByCollegeAndVisaMan(collegeId, vm.getId());
            vmMetrics.setStudentsHandled(count);
            // Count visa stamped
            vmMetrics.setStudentsCompleted(count); // Simplified
            visaMetrics.add(vmMetrics);
        }
        performance.setVisaMen(visaMetrics);

        // DocPerson - get active doc person
        List<DocPerson> docPersons = docPersonRepository.findAll();
        DocPerson activeDocPerson = docPersons.stream()
                .filter(dp -> "Active".equalsIgnoreCase(dp.getStatus()))
                .findFirst().orElse(null);
        if (activeDocPerson != null) {
            EntityMetrics dpMetrics = new EntityMetrics(activeDocPerson.getId(),
                    activeDocPerson.getUsername(), "Document Person");
            dpMetrics.setStatus(activeDocPerson.getStatus());
            long count = studentRepository.countByCollegeAndDocPerson(collegeId, activeDocPerson.getId());
            dpMetrics.setStudentsHandled(count);
            performance.setDocPerson(dpMetrics);
        }

        // Accountant - get active accountant
        List<Accountant> accountants = accountantRepository.findAll();
        Accountant activeAccountant = accountants.stream()
                .filter(acc -> "Active".equalsIgnoreCase(acc.getStatus()))
                .findFirst().orElse(null);
        if (activeAccountant != null) {
            EntityMetrics accMetrics = new EntityMetrics(activeAccountant.getId(),
                    activeAccountant.getUsername(), "Accountant");
            accMetrics.setStatus(activeAccountant.getStatus());
            long count = studentRepository.countByCollegeAndAccountant(collegeId, activeAccountant.getId());
            accMetrics.setStudentsHandled(count);
            performance.setAccountant(accMetrics);
        }

        return performance;
    }

    private CollegeTimeSeriesDTO buildTimeSeries(Long collegeId, LocalDate dateFrom, LocalDate dateTo) {
        CollegeTimeSeriesDTO timeSeries = new CollegeTimeSeriesDTO();
        timeSeries.setDateFrom(dateFrom);
        timeSeries.setDateTo(dateTo);

        // Determine granularity based on date range
        long daysBetween = ChronoUnit.DAYS.between(dateFrom, dateTo);
        String granularity = daysBetween > 60 ? "weekly" : "daily";
        timeSeries.setGranularity(granularity);

        // Get all history entries in date range
        LocalDateTime startDateTime = dateFrom.atStartOfDay();
        LocalDateTime endDateTime = dateTo.atTime(23, 59, 59);
        List<StudentStatusHistory> historyEntries = historyRepository.findByCollegeAndDateRange(
                collegeId, startDateTime, endDateTime);

        // Group by date
        Map<LocalDate, CollegeDataPoint> dataMap = new LinkedHashMap<>();
        LocalDate current = dateFrom;
        while (!current.isAfter(dateTo)) {
            dataMap.put(current, new CollegeDataPoint(current));
            current = current.plusDays(1);
        }

        // Process history entries
        for (StudentStatusHistory h : historyEntries) {
            LocalDate date = h.getUpdatedAt().toLocalDate();
            CollegeDataPoint point = dataMap.get(date);
            if (point == null)
                continue;

            StudentStatus status = h.getStudentApplicationStatus();

            if (status == StudentStatus.REGISTERED || status == StudentStatus.Submitted) {
                point.setNewRegistrations(point.getNewRegistrations() + 1);
            } else if (status == StudentStatus.ADMISSION_LETTER_GENERATED) {
                point.setAdmissionLettersGenerated(point.getAdmissionLettersGenerated() + 1);
            } else if (status == StudentStatus.TELEX_NUMBER_ISSUED) {
                point.setTelexIssued(point.getTelexIssued() + 1);
            } else if (status == StudentStatus.VISA_STAMPED) {
                point.setVisaStamped(point.getVisaStamped() + 1);
            } else if (status == StudentStatus.REJECTED) {
                point.setRejected(point.getRejected() + 1);
            }
        }

        // Also add new registrations based on createdAt
        List<Student> students = studentRepository.findByCollege_Id(collegeId);
        for (Student s : students) {
            if (s.getCreatedAt() != null && !s.getCreatedAt().isBefore(dateFrom)
                    && !s.getCreatedAt().isAfter(dateTo)) {
                CollegeDataPoint point = dataMap.get(s.getCreatedAt());
                if (point != null) {
                    point.setNewRegistrations(point.getNewRegistrations() + 1);
                }
            }
        }

        timeSeries.setData(new ArrayList<>(dataMap.values()));
        return timeSeries;
    }

    private List<CollegeRecentActivityDTO> getRecentActivities(Long collegeId, int limit) {
        List<StudentStatusHistory> recentHistory = historyRepository.findRecentActivitiesByCollegeId(
                collegeId, PageRequest.of(0, limit));

        List<CollegeRecentActivityDTO> activities = new ArrayList<>();

        for (StudentStatusHistory h : recentHistory) {
            CollegeRecentActivityDTO activity = new CollegeRecentActivityDTO();
            activity.setActivityId(h.getId());
            activity.setStudentId(h.getStudent().getId());
            activity.setStudentName(h.getStudent().getFullName());
            activity.setApplicationId(h.getStudent().getApplicationId());
            activity.setNewStatus(h.getStudentApplicationStatus().name());
            activity.setUpdatedBy(h.getUpdatedBy());
            activity.setUpdatedAt(h.getUpdatedAt());
            activity.setRemarks(h.getRemarks());
            activities.add(activity);
        }

        return activities;
    }

    private List<CollegeAlertDTO> generateAlerts(Long collegeId) {
        List<CollegeAlertDTO> alerts = new ArrayList<>();

        // Alert 1: Students stuck without progress for more than 7 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        List<Student> stuckStudents = historyRepository.findStuckStudentsByCollegeId(
                collegeId, cutoffDate, COMPLETED_STATUSES);

        // Filter to actually stuck students (last update was before cutoff)
        List<Student> actuallyStuck = new ArrayList<>();
        for (Student s : stuckStudents) {
            List<StudentStatusHistory> history = historyRepository.findByStudentId(s.getId());
            if (!history.isEmpty()) {
                StudentStatusHistory lastUpdate = history.get(history.size() - 1);
                if (lastUpdate.getUpdatedAt().isBefore(cutoffDate)
                        && !COMPLETED_STATUSES.contains(s.getApplicationStatus())
                        && s.getApplicationStatus() != StudentStatus.REJECTED) {
                    actuallyStuck.add(s);
                }
            }
        }

        if (!actuallyStuck.isEmpty()) {
            CollegeAlertDTO stuckAlert = new CollegeAlertDTO(
                    "STUCK_STUDENT", "HIGH",
                    "Students Stuck > 7 Days",
                    actuallyStuck.size() + " student(s) haven't progressed in over 7 days",
                    (long) actuallyStuck.size(), null, null);
            alerts.add(stuckAlert);
        }

        // Alert 2: Pending payments
        List<Student> collegeStudents = studentRepository.findByCollege_Id(collegeId);
        long pendingPayments = collegeStudents.stream()
                .filter(s -> "Pending".equalsIgnoreCase(s.getPaymentStatus()))
                .count();

        if (pendingPayments > 0) {
            CollegeAlertDTO paymentAlert = new CollegeAlertDTO(
                    "PENDING_PAYMENT", "MEDIUM",
                    "Pending Payments",
                    pendingPayments + " student(s) have pending payments",
                    pendingPayments, null, null);
            alerts.add(paymentAlert);
        }

        // Alert 3: Documents pending
        long docsPending = collegeStudents.stream()
                .filter(s -> Boolean.TRUE.equals(s.getOriginalDocsSent())
                        && s.getDocReceivedDate() == null)
                .count();

        if (docsPending > 0) {
            CollegeAlertDTO docsAlert = new CollegeAlertDTO(
                    "DOCUMENT_ISSUE", "MEDIUM",
                    "Documents Pending Receipt",
                    docsPending + " student(s) with documents in transit",
                    docsPending, null, null);
            alerts.add(docsAlert);
        }

        return alerts;
    }
}
