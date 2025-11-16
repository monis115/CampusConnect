package com.cc.repository;

import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Existing methods...
    List<Student> findByAgent_Id(Long agentId);
    List<Student> findByApplicationStatus(StudentStatus applicationStatus);
    List<Student> findByUpdatedByAndApplicationStatus(String updatedBy, StudentStatus applicationStatus);

    // ✅ NEW METHODS FOR MANAGER FILTERS
    // Finds students assigned to a specific manager with a specific status (e.g., APPROVED)
    List<Student> findByAdmissionManagerAndApplicationStatus(AddmissionManager admissionManager, StudentStatus applicationStatus);

    // Finds students assigned to a specific manager with ANY of the statuses in the list
    List<Student> findByAdmissionManagerAndApplicationStatusIn(AddmissionManager admissionManager, List<StudentStatus> applicationStatuses);
}