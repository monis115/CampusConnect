package com.cc.repository;

import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByAgent_Id(Long agentId);

    List<Student> findByApplicationStatus(String applicationStatus);

    List<Student> findByPresentStatus(StudentStatus presentStatus);
    List<Student> findByAdmissionManagerAndPresentStatus(
    	    AddmissionManager manager, StudentStatus presentStatus);
    
    List<Student> findByAdmissionManagerAndPresentStatusIn(
            AddmissionManager manager, List<StudentStatus> statuses);
    
    List<Student> findByAdmissionManager_UsernameAndPresentStatus(
            String managerUsername,
            StudentStatus presentStatus);
    
    
}
