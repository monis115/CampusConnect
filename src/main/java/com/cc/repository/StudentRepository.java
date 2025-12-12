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

        // Recent Activity
        List<Student> findTop5ByAdmissionManagerOrderByIdDesc(AddmissionManager admissionManager);

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
}