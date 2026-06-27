package com.cc.repository;

import com.cc.modals.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN t.watchers w " +
            "LEFT JOIN t.student s " +
            "LEFT JOIN s.admissionManager am " +
            "LEFT JOIN s.visaMan vm " +
            "LEFT JOIN s.telexMan tm " +
            "LEFT JOIN s.docPerson dp " +
            "LEFT JOIN s.accountant acc " +
            "LEFT JOIN s.universityPerson up " +
            "LEFT JOIN s.agent ag " +
            "WHERE " +
            "t.status != 'SCHEDULED' AND (" +
            "(:role = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.createdBy = :username) OR " +
            "(w = :username) OR " +
            "(t.escalatedTo = :username OR t.escalatedTo = :role) OR " +
            "(t.assignedToUser = :username) OR " +
            "(t.assignedToRole = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.assignedToRole = 'VISA_MANAGER' AND vm.username = :username) OR " +
            "(t.assignedToRole = 'TELEX_MANAGER' AND tm.username = :username) OR " +
            "(t.assignedToRole = 'DOCUMENT_MANAGER' AND dp.username = :username) OR " +
            "(t.assignedToRole = 'ACCOUNTANT' AND acc.username = :username) OR " +
            "(t.assignedToRole = 'UNIVERSITY_COORDINATOR' AND up.userName = :username) OR " +
            "(t.assignedToRole = 'AGENT' AND ag.userId = :username))")
    List<Task> findEnterpriseDashboardTasks(@Param("username") String username, @Param("role") String role);

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN t.watchers w " +
            "LEFT JOIN t.student s " +
            "LEFT JOIN s.admissionManager am " +
            "LEFT JOIN s.visaMan vm " +
            "LEFT JOIN s.telexMan tm " +
            "LEFT JOIN s.docPerson dp " +
            "LEFT JOIN s.accountant acc " +
            "LEFT JOIN s.universityPerson up " +
            "LEFT JOIN s.agent ag " +
            "WHERE " +
            "t.status != 'SCHEDULED' AND (" +
            "(:role = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.createdBy = :username) OR " +
            "(w = :username) OR " +
            "(t.escalatedTo = :username OR t.escalatedTo = :role) OR " +
            "(t.assignedToUser = :username) OR " +
            "(t.assignedToRole = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.assignedToRole = 'VISA_MANAGER' AND vm.username = :username) OR " +
            "(t.assignedToRole = 'TELEX_MANAGER' AND tm.username = :username) OR " +
            "(t.assignedToRole = 'DOCUMENT_MANAGER' AND dp.username = :username) OR " +
            "(t.assignedToRole = 'ACCOUNTANT' AND acc.username = :username) OR " +
            "(t.assignedToRole = 'UNIVERSITY_COORDINATOR' AND up.userName = :username) OR " +
            "(t.assignedToRole = 'AGENT' AND ag.userId = :username)) AND " +
            "(:statusFilter = 'all' OR " +
            " (:statusFilter = 'my' AND t.status NOT IN ('COMPLETED', 'AWAITING_APPROVAL')) OR " +
            " (:statusFilter = 'pending-approval' AND t.status = 'AWAITING_APPROVAL') OR " +
            " (:statusFilter = 'overdue' AND t.dueDate < CURRENT_DATE AND t.status != 'COMPLETED')) AND " +
            "(:assignedBy IS NULL OR :assignedBy = '' OR t.createdBy = :assignedBy) AND " +
            "(:assignedTo IS NULL OR :assignedTo = '' OR t.assignedToRole = :assignedTo OR t.assignedToUser = :assignedTo) AND " +
            "(:department IS NULL OR :department = '' OR t.department = :department)")
    org.springframework.data.domain.Page<Task> findEnterpriseDashboardTasksPaginated(
            @Param("username") String username, 
            @Param("role") String role, 
            @Param("statusFilter") String statusFilter, 
            @Param("assignedBy") String assignedBy,
            @Param("assignedTo") String assignedTo,
            @Param("department") String department,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(DISTINCT t) FROM Task t " +
            "LEFT JOIN t.watchers w " +
            "LEFT JOIN t.student s " +
            "LEFT JOIN s.admissionManager am " +
            "LEFT JOIN s.visaMan vm " +
            "LEFT JOIN s.telexMan tm " +
            "LEFT JOIN s.docPerson dp " +
            "LEFT JOIN s.accountant acc " +
            "LEFT JOIN s.universityPerson up " +
            "LEFT JOIN s.agent ag " +
            "WHERE " +
            "t.status != 'SCHEDULED' AND (" +
            "(:role = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.createdBy = :username) OR " +
            "(w = :username) OR " +
            "(t.escalatedTo = :username OR t.escalatedTo = :role) OR " +
            "(t.assignedToUser = :username) OR " +
            "(t.assignedToRole = 'ADMISSION_MANAGER' AND am.username = :username) OR " +
            "(t.assignedToRole = 'VISA_MANAGER' AND vm.username = :username) OR " +
            "(t.assignedToRole = 'TELEX_MANAGER' AND tm.username = :username) OR " +
            "(t.assignedToRole = 'DOCUMENT_MANAGER' AND dp.username = :username) OR " +
            "(t.assignedToRole = 'ACCOUNTANT' AND acc.username = :username) OR " +
            "(t.assignedToRole = 'UNIVERSITY_COORDINATOR' AND up.userName = :username) OR " +
            "(t.assignedToRole = 'AGENT' AND ag.userId = :username)) AND " +
            "(:statusFilter = 'all' OR " +
            " (:statusFilter = 'my' AND t.status NOT IN ('COMPLETED', 'AWAITING_APPROVAL')) OR " +
            " (:statusFilter = 'pending-approval' AND t.status = 'AWAITING_APPROVAL') OR " +
            " (:statusFilter = 'overdue' AND t.dueDate < CURRENT_DATE AND t.status != 'COMPLETED')) AND " +
            "(:assignedBy IS NULL OR :assignedBy = '' OR t.createdBy = :assignedBy) AND " +
            "(:assignedTo IS NULL OR :assignedTo = '' OR t.assignedToRole = :assignedTo OR t.assignedToUser = :assignedTo) AND " +
            "(:department IS NULL OR :department = '' OR t.department = :department)")
    long countEnterpriseDashboardTasks(
            @Param("username") String username, 
            @Param("role") String role, 
            @Param("statusFilter") String statusFilter,
            @Param("assignedBy") String assignedBy,
            @Param("assignedTo") String assignedTo,
            @Param("department") String department);

    @Query("SELECT t FROM Task t WHERE t.student.id = :studentId AND t.title = :title AND t.status != 'COMPLETED'")
    List<Task> findActiveSystemTasksByStudentAndTitle(@Param("studentId") Long studentId, @Param("title") String title);
    
    @Query("SELECT t FROM Task t WHERE t.status != 'COMPLETED' AND t.status != 'AWAITING_APPROVAL' AND t.status != 'SCHEDULED'")
    List<Task> findAllActiveTasks();

    @Query("SELECT t FROM Task t WHERE t.systemGenerated = true AND t.status NOT IN ('CANCELLED', 'COMPLETED') AND (t.assignedToUser IS NULL OR t.assignedToUser = '' OR t.assignedToUser = '-')")
    List<Task> findOrphanedSystemTasks();

    // Duplicate prevention with rule code
    @Query("SELECT t FROM Task t WHERE t.student.id = :studentId AND t.ruleCode = :ruleCode AND t.status NOT IN ('COMPLETED', 'VERIFIED', 'ARCHIVED')")
    List<Task> findActiveByStudentAndRuleCode(@Param("studentId") Long studentId, @Param("ruleCode") String ruleCode);
    
    // Scheduled Tasks Activation
    List<Task> findByStatusAndScheduledForLessThanEqual(String status, java.time.LocalDateTime time);

    // Dashboard Intelligence
    List<Task> findByAssignedToUserAndStatusNotIn(String username, List<String> statuses);
    List<Task> findByDepartmentAndStatusNotIn(String department, List<String> statuses);
    List<Task> findByStatus(String status);
    List<Task> findByDueDateAndStatusNot(LocalDate date, String status);
    List<Task> findByDueDateBetweenAndStatusNot(LocalDate start, LocalDate end, String status);
    List<Task> findByAssignedToUserAndStatus(String username, String status);

    // Workload distribution
    @Query("SELECT t.assignedToUser, COUNT(t) FROM Task t WHERE t.status NOT IN ('COMPLETED','VERIFIED','ARCHIVED') AND t.assignedToUser IS NOT NULL GROUP BY t.assignedToUser")
    List<Object[]> findWorkloadDistribution();

    // Analytics
    @Query("SELECT t.department, COUNT(t) FROM Task t WHERE t.status NOT IN ('COMPLETED','VERIFIED','ARCHIVED') GROUP BY t.department")
    List<Object[]> countOpenTasksByDepartment();

    // Overdue tasks for reminders
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status NOT IN ('COMPLETED','VERIFIED','ARCHIVED','CLOSED')")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // Tasks approaching SLA
    @Query("SELECT t FROM Task t WHERE t.dueDate = :date AND t.status NOT IN ('COMPLETED','VERIFIED','ARCHIVED','CLOSED')")
    List<Task> findTasksDueOn(@Param("date") LocalDate date);

    // Analytics queries
    @Query("SELECT t FROM Task t WHERE t.status = 'CLOSED' AND t.closedAt >= :startDate AND t.closedAt <= :endDate")
    List<Task> findClosedTasksInRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

}
