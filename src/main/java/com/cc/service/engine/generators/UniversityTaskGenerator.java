package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class UniversityTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "UNIVERSITY";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: University tasks become active only after Coordinator is assigned
        return student.getUniversityPerson() != null;
    }
}
