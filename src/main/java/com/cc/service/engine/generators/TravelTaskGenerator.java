package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class TravelTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "TRAVEL";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: Visa is stamped/completed OR Travel date is already assigned
        return student.getVisaTalabNumaPath() != null || student.getTravelDoj() != null;
    }
}
