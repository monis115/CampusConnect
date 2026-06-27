package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class DocumentTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "DOCUMENT";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: Document Manager assigned & Passport/Visa operations active
        return student.getDocPerson() != null && 
               (student.getPassportValidationDate() != null || 
                student.getVisaTalabNumaPath() != null || 
                Boolean.TRUE.equals(student.getHasRaiseReturnRequest()));
    }
}
