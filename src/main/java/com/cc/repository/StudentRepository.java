package com.cc.repository;

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
}
