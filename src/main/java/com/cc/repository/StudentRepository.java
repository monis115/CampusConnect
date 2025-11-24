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

        @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s WHERE s.universityPerson.userName = :username AND s.applicationStatus IN ("
                        +
                        "com.cc.modals.StudentStatus.ADMISSION_LETTER_GENERATED, " +
                        "com.cc.modals.StudentStatus.ADMISSION_LETTER_APPROVED, " +
                        "com.cc.modals.StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED, " +
                        "com.cc.modals.StudentStatus.APPLIED_FOR_TELEX, " +
                        "com.cc.modals.StudentStatus.TELEX_FEE_RECIEVED, " +
                        "com.cc.modals.StudentStatus.TELEX_INITIATED, " +
                        "com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY, " +
                        "com.cc.modals.StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA, " +
                        "com.cc.modals.StudentStatus.TELEX_NUMBER_ISSUED, " +
                        "com.cc.modals.StudentStatus.DOCS_VARIFICATION_COMPLETE, " +
                        "com.cc.modals.StudentStatus.DOCS_VARIFICATION_APPROVED, " +
                        "com.cc.modals.StudentStatus.VISA_FEE_PENDING, " +
                        "com.cc.modals.StudentStatus.VISA_FEE_RECEIVED, " +
                        "com.cc.modals.StudentStatus.VISA_APPLIED, " +
                        "com.cc.modals.StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY, " +
                        "com.cc.modals.StudentStatus.VISA_STAMPED, " +
                        "com.cc.modals.StudentStatus.COMPLETED)")
        List<Student> findAdmittedStudentsByUniversityPerson(
                        @org.springframework.data.repository.query.Param("username") String username);
}