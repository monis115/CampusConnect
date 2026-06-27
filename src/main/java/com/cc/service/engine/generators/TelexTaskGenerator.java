package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class TelexTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "TELEX";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: Admission Letter uploaded & Telex Manager assigned
        return student.getAdmissionLetterPath() != null && student.getTelexMan() != null;
    }
}
