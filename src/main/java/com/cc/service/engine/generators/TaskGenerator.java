package com.cc.service.engine.generators;

import com.cc.modals.Student;
import com.cc.modals.TaskRule;

import java.util.List;

/**
 * Interface for modular, department-specific task generators.
 * Each implementation handles only its own department's business logic.
 */
public interface TaskGenerator {

    /**
     * Returns the department this generator handles.
     */
    String getDepartment();

    /**
     * Evaluate all rules for a given student within this department.
     */
    void evaluate(Student student, List<TaskRule> departmentRules);
}
