package com.cc.repository;

import com.cc.modals.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
        List<StudentStatusHistory> findByStudentId(Long studentId);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.agent.id = :agentId ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findRecentActivitiesByAgentId(
                        @org.springframework.data.repository.query.Param("agentId") Long agentId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.agent.userId = :username ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findRecentActivitiesByAgentUsername(
                        @org.springframework.data.repository.query.Param("username") String username,
                        org.springframework.data.domain.Pageable pageable);

        List<StudentStatusHistory> findByUpdatedByContainingOrderByUpdatedAtDesc(String username,
                        org.springframework.data.domain.Pageable pageable);

        // ======================= COLLEGE PERFORMANCE DASHBOARD METHODS
        // =======================

        // Recent activities for a college
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.college.id = :collegeId ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findRecentActivitiesByCollegeId(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        org.springframework.data.domain.Pageable pageable);

        // Count status transitions within date range for a college
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(h) FROM StudentStatusHistory h WHERE h.student.college.id = :collegeId AND h.studentApplicationStatus = :status AND h.updatedAt BETWEEN :start AND :end")
        long countStatusTransitionsByCollegeAndDateRange(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("status") com.cc.modals.StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Find all history entries for a college within date range (for time series)
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.college.id = :collegeId AND h.updatedAt BETWEEN :start AND :end ORDER BY h.updatedAt ASC")
        List<StudentStatusHistory> findByCollegeAndDateRange(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Find students with no status change for N days (for alerts)
        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT h.student FROM StudentStatusHistory h WHERE h.student.college.id = :collegeId AND h.updatedAt < :cutoffDate AND h.student.applicationStatus NOT IN :completedStatuses")
        List<com.cc.modals.Student> findStuckStudentsByCollegeId(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("cutoffDate") java.time.LocalDateTime cutoffDate,
                        @org.springframework.data.repository.query.Param("completedStatuses") java.util.List<com.cc.modals.StudentStatus> completedStatuses);

        // ======================= ADMISSION MANAGER REPORT METHODS
        // =======================

        // Find all history entries for students under a specific admission manager
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.admissionManager.id = :managerId ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findByAdmissionManagerId(
                        @org.springframework.data.repository.query.Param("managerId") Long managerId);

        // Find history entries for students under a manager within date range
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.admissionManager.id = :managerId AND h.updatedAt BETWEEN :start AND :end ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findByAdmissionManagerIdAndDateRange(
                        @org.springframework.data.repository.query.Param("managerId") Long managerId,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // ======================= UNIVERSITY PERSON REPORT METHODS
        // =======================

        // Find all history entries for students assigned to a specific UniversityPerson
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.universityPerson.id = :universityPersonId ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findByUniversityPersonId(
                        @org.springframework.data.repository.query.Param("universityPersonId") Long universityPersonId);

        // Find history entries for students assigned to a UniversityPerson within date
        // range
        @org.springframework.data.jpa.repository.Query("SELECT h FROM StudentStatusHistory h WHERE h.student.universityPerson.id = :universityPersonId AND h.updatedAt BETWEEN :start AND :end ORDER BY h.updatedAt DESC")
        List<StudentStatusHistory> findByUniversityPersonIdAndDateRange(
                        @org.springframework.data.repository.query.Param("universityPersonId") Long universityPersonId,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // ======================= AGENT REPORTING QUERIES =======================

        // Count Telex Initiated (TELEX_PAYMENT_DOCS_SUBMITTED) by Agent
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(h) FROM StudentStatusHistory h WHERE h.updatedBy = :username AND h.studentApplicationStatus = :status AND h.updatedAt BETWEEN :start AND :end")
        long countByUpdatedByAndStatusAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("status") com.cc.modals.StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Count Activities by Remarks (Payment Confirmation and Doc Requests)
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(h) FROM StudentStatusHistory h WHERE h.updatedBy = :username AND h.remarks LIKE %:remarkFragment% AND h.updatedAt BETWEEN :start AND :end")
        long countByUpdatedByAndRemarksContainingAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("remarkFragment") String remarkFragment,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

}
