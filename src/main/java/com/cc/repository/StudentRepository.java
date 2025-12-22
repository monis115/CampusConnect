package com.cc.repository;

import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

        boolean existsByApplicationId(String applicationId);

        java.util.Optional<Student> findByApplicationId(String applicationId);

        java.util.Optional<Student> findByEmail(String email);

        // Existing methods...
        List<Student> findByAgent_Id(Long agentId);

        List<Student> findByApplicationStatus(StudentStatus applicationStatus);

        List<Student> findByUpdatedByAndApplicationStatus(String updatedBy, StudentStatus applicationStatus);

        // ✅ NEW METHODS FOR MANAGER FILTERS
        // Finds students assigned to a specific manager with a specific status (e.g.,
        // APPROVED)
        List<Student> findByAdmissionManagerAndApplicationStatus(AddmissionManager admissionManager,
                        StudentStatus applicationStatus);

        // Finds students assigned to a specific manager with ANY of the statuses in the
        // list
        List<Student> findByAdmissionManagerAndApplicationStatusIn(AddmissionManager admissionManager,
                        List<StudentStatus> applicationStatuses);

        // Count methods for Dashboard
        long countByAdmissionManager(AddmissionManager admissionManager);

        long countByAdmissionManagerAndApplicationStatusIn(AddmissionManager admissionManager,
                        List<StudentStatus> applicationStatuses);

        long countByAdmissionManagerAndApplicationStatus(AddmissionManager admissionManager,
                        StudentStatus applicationStatus);

        // Count students with TelexMan/VisaMan assigned for a specific Admission
        // Manager
        long countByAdmissionManagerAndTelexManIsNotNull(AddmissionManager admissionManager);

        long countByAdmissionManagerAndVisaManIsNotNull(AddmissionManager admissionManager);

        // Recent Activity
        List<Student> findTop5ByAdmissionManagerOrderByIdDesc(AddmissionManager admissionManager);

        // Find all students for a manager
        List<Student> findByAdmissionManager(AddmissionManager admissionManager);

        // Find students for a manager sorted by creation date descending (newest first)
        List<Student> findByAdmissionManagerOrderByCreatedAtDesc(AddmissionManager admissionManager);

        @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s WHERE s.universityPerson.userName = :username AND s.applicationStatus IN :statuses")
        List<Student> findAdmittedStudentsByUniversityPerson(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("statuses") List<StudentStatus> statuses);

        default List<Student> findAdmittedStudentsByUniversityPerson(String username) {
                return findAdmittedStudentsByUniversityPerson(username, List.of(
                                StudentStatus.ADMISSION_LETTER_GENERATED,
                                StudentStatus.ADMISSION_LETTER_APPROVED,
                                StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED,
                                StudentStatus.APPLIED_FOR_TELEX,
                                StudentStatus.TELEX_FEE_RECIEVED,
                                StudentStatus.TELEX_INITIATED,
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY,
                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA,
                                StudentStatus.TELEX_NUMBER_ISSUED,
                                StudentStatus.DOCS_VARIFICATION_COMPLETE,
                                StudentStatus.DOCS_VARIFICATION_APPROVED,
                                StudentStatus.VISA_FEE_PENDING,
                                StudentStatus.VISA_FEE_RECEIVED,
                                StudentStatus.VISA_APPLIED,
                                StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY,
                                StudentStatus.VISA_STAMPED,
                                StudentStatus.COMPLETED));
        }

        // ======================= DOC PERSON REPORT METHODS =======================

        // Count assigned students (All Time - or potentially period based if we use the
        // NEW field)
        long countByDocPerson_Username(String username);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.docPerson.username = :username AND "
                        + "COALESCE(s.docPersonAssignedDate, s.docReceivedDate) BETWEEN :start AND :end")
        long countByDocPerson_UsernameAndDocPersonAssignedDateBetween(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDate start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDate end);

        // 1. Received Docs: Based on docReceivedDate
        long countByDocPerson_UsernameAndDocReceivedDateBetween(String username, java.time.LocalDate start,
                        java.time.LocalDate end);

        // 2. Verified Docs: Based on History Status 'TELEX_INITIATED' + UpdatedBy check
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.docPerson.username = :username " +
                        "AND h.studentApplicationStatus = com.cc.modals.StudentStatus.TELEX_INITIATED " +
                        "AND LOWER(h.updatedBy) LIKE LOWER(CONCAT('%', :username, '%')) " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByDocPersonAndVerifiedDocsDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // 3. Collected Docs: Based on History "Collected by [username]"
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.docPerson.username = :username " +
                        "AND LOWER(h.updatedBy) LIKE LOWER(CONCAT('Collected by ', :username, '%')) " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByDocPersonAndCollectedDocsDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // 4. Returned Docs: Based on docReturnedDate
        long countByDocPerson_UsernameAndDocReturnedDateBetween(String username, java.time.LocalDate start,
                        java.time.LocalDate end);

        // ======================= ADMIN DASHBOARD METHODS =======================

        // Count by single status
        long countByApplicationStatus(StudentStatus status);

        // Count by multiple statuses
        long countByApplicationStatusIn(List<StudentStatus> statuses);

        // Count by date range (for new registrations)
        long countByCreatedAtAfter(java.time.LocalDate date);

        long countByCreatedAtBetween(java.time.LocalDate start, java.time.LocalDate end);

        // Recent students ordered by ID descending
        List<Student> findTop10ByOrderByIdDesc();

        // Students with payment status
        long countByPaymentStatus(String paymentStatus);

        // Document tracking counts
        long countByDocReceivedDateIsNotNull();

        long countByDocReturnedDateIsNotNull();

        // Telex counts
        long countByTelexNumberIsNotNull();

        // Find students with admission letters, newest first
        List<Student> findByAdmissionLetterPathIsNotNullOrderByIdDesc();

        // ======================= COLLEGE PERFORMANCE DASHBOARD METHODS
        // =======================

        // Count by college
        long countByCollege_Id(Long collegeId);

        long countByCollege_IdAndApplicationStatus(Long collegeId, StudentStatus status);

        long countByCollege_IdAndApplicationStatusIn(Long collegeId, List<StudentStatus> statuses);

        // Date range filtering with college
        long countByCollege_IdAndCreatedAtBetween(Long collegeId, java.time.LocalDate start, java.time.LocalDate end);

        // Find students by college
        List<Student> findByCollege_Id(Long collegeId);

        List<Student> findByCollege_IdAndApplicationStatusIn(Long collegeId, List<StudentStatus> statuses);

        List<Student> findByCollege_IdOrderByCreatedAtDesc(Long collegeId);

        // Entity-specific counts for a college
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.college.id = :collegeId AND s.telexMan.id = :telexManId")
        long countByCollegeAndTelexMan(@org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("telexManId") Long telexManId);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.college.id = :collegeId AND s.visaMan.id = :visaManId")
        long countByCollegeAndVisaMan(@org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("visaManId") Long visaManId);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.college.id = :collegeId AND s.docPerson.id = :docPersonId")
        long countByCollegeAndDocPerson(@org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("docPersonId") Long docPersonId);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.college.id = :collegeId AND s.accountant.id = :accountantId")
        long countByCollegeAndAccountant(@org.springframework.data.repository.query.Param("collegeId") Long collegeId,
                        @org.springframework.data.repository.query.Param("accountantId") Long accountantId);

        // Distinct entity counts for a college
        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.telexMan FROM Student s WHERE s.college.id = :collegeId AND s.telexMan IS NOT NULL")
        List<com.cc.modals.TelexMan> findDistinctTelexMenByCollegeId(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId);

        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.visaMan FROM Student s WHERE s.college.id = :collegeId AND s.visaMan IS NOT NULL")
        List<com.cc.modals.VisaMan> findDistinctVisaMenByCollegeId(
                        @org.springframework.data.repository.query.Param("collegeId") Long collegeId);

        List<Student> findByCollege_IdAndCreatedAtBetween(Long collegeId, java.time.LocalDate start,
                        java.time.LocalDate end);

        // ======================= UNIVERSITY PERSON REPORT METHODS
        // =======================

        // Count students assigned to a specific UniversityPerson
        long countByUniversityPerson_Id(Long universityPersonId);

        // Find students by UniversityPerson ID
        List<Student> findByUniversityPerson_Id(Long universityPersonId);

        // ======================= GLOBAL BENCHMARKING METHODS =======================
        long countByApplicationStatusInAndCreatedAtBetween(List<StudentStatus> statuses, java.time.LocalDate start,
                        java.time.LocalDate end);

        long countByApplicationStatusAndCreatedAtBetween(StudentStatus status, java.time.LocalDate start,
                        java.time.LocalDate end);

        List<Student> findByApplicationStatusInAndCreatedAtBetween(List<StudentStatus> statuses,
                        java.time.LocalDate start, java.time.LocalDate end);

        // ======================= TELEX MANAGER REPORT METHODS =======================

        // Count total students assigned to a Telex Manager
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.telexMan.username = :username")
        long countByTelexManUsername(@org.springframework.data.repository.query.Param("username") String username);

        // Count status changes by specific manager within date range
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.telexMan.username = :username " +
                        "AND h.studentApplicationStatus = :status " +
                        "AND h.updatedBy = :username " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByTelexManAndStatusAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Special case for "Submitted Documents" which uses specific status OR remarks
        // if needed.
        // Based on logic: Upload Telex Docs -> sets status to
        // SUBMIT_TELEX_TO_UNIVERSITY (next action)
        // Actually, looking at service: uploadTelexDocuments -> Action:
        // SUBMIT_TELEX_TO_UNIVERSITY, Status remains...
        // Wait, uploadTelexDocuments does NOT change status?
        // Service line 136: addHistory(student, student.getApplicationStatus(), ...);
        // It uses current status but adds history "Uploaded Documents".

        // Count specific "Document Uploaded" history entries
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.telexMan.username = :username " +
                        "AND (LOWER(h.remarks) LIKE '%upload%' OR LOWER(h.remarks) LIKE '%document%') " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countTelexDocsUploadedByManager(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // ======================= GLOBAL TELEX STATS (FOR ADMIN)
        // =======================

        // Count total students assigned to ANY TelexMan
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.telexMan IS NOT NULL")
        long countAllTelexManStudents();

        // Count status changes by ANY telex manager within date range
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.telexMan IS NOT NULL " +
                        "AND h.studentApplicationStatus = :status " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countAllTelexStatusByDateRange(
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Count all document uploads for ALL telex managers
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.telexMan IS NOT NULL " +
                        "AND h.remarks LIKE '%Uploaded Documents%' " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countAllTelexDocsUploaded(
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Count students by TelexMan username with status in list
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.telexMan.username = :username AND s.applicationStatus IN :statuses")
        long countByTelexManUsernameAndStatusIn(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("statuses") java.util.List<StudentStatus> statuses);

        // Count students assigned to TelexMan within date range (based on history
        // status, not updatedBy)
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.telexMan.username = :username " +
                        "AND h.studentApplicationStatus = :status " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countAssignedToTelexManByStatusAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // ======================= VISA MANAGER REPORT METHODS =======================

        // Count by VisaMan
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Student s WHERE s.visaMan.username = :username")
        long countByVisaManUsername(@org.springframework.data.repository.query.Param("username") String username);

        // Count status changes by specific VisaMan within date range
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.visaMan.username = :username " +
                        "AND h.studentApplicationStatus = :status " +
                        "AND h.updatedBy = :username " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByVisaManAndStatusAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        List<Student> findByVisaMan_Username(String username);

        // Count history remarks by specific VisaMan within date range (For Payment
        // Requests, etc.)
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE h.updatedBy = :username "
                        + "AND LOWER(h.remarks) LIKE LOWER(CONCAT('%', :remarkPattern, '%')) "
                        + "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByVisaManAndHistoryRemarkAndDateRange(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("remarkPattern") String remarkPattern,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Count students TRANSITIONED to a status while assigned to this VisaMan,
        // IGNORING WHO updated it.
        // Useful for "Assigned To" counts where AdmissionManager does the updating.
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        + "WHERE s.visaMan.username = :username "
                        + "AND h.studentApplicationStatus = :status "
                        + "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countByVisaManAndStatusAndDateRangeNoUpdateUser(
                        @org.springframework.data.repository.query.Param("username") String username,
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // ======================= GLOBAL VISA STATS (FOR ADMIN) =======================

        // Count status changes by ANY visa manager within date range
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE s.visaMan IS NOT NULL " +
                        "AND h.studentApplicationStatus = :status " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countAllVisaStatusByDateRange(
                        @org.springframework.data.repository.query.Param("status") StudentStatus status,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

        // Count remarks by ANY visa manager within date range
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.studentStatusHistory h "
                        +
                        "WHERE LOWER(h.remarks) LIKE LOWER(CONCAT('%', :remarkPattern, '%')) " +
                        "AND h.updatedAt >= :start AND h.updatedAt <= :end")
        long countAllVisaHistoryRemarkByDateRange(
                        @org.springframework.data.repository.query.Param("remarkPattern") String remarkPattern,
                        @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                        @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

}