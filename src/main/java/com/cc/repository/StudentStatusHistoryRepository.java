package com.cc.repository;

import com.cc.modals.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudentId(Long studentId);
    
}
