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

        // ======================= GLOBAL BENCHMARKING METHODS =======================
        long countByApplicationStatusInAndCreatedAtBetween(List<StudentStatus> statuses, java.time.LocalDate start,
                        java.time.LocalDate end);

        long countByApplicationStatusAndCreatedAtBetween(StudentStatus status, java.time.LocalDate start,
                        java.time.LocalDate end);

        List<Student> findByApplicationStatusInAndCreatedAtBetween(List<StudentStatus> statuses,
                        java.time.LocalDate start, java.time.LocalDate end);
}