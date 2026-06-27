package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class VisaTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "VISA";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: Telex completed (number issued) & Visa Manager assigned
        return student.getTelexNumber() != null && student.getVisaMan() != null;
    }
}
