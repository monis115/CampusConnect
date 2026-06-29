package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class AgentTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "AGENT";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: An agent is assigned to the student
        return student.getAgent() != null;
    }
}
