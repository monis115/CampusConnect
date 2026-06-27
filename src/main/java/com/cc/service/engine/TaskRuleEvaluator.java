package com.cc.service.engine;

import com.cc.modals.Student;
import com.cc.modals.TaskRule;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * Evaluates TaskRule conditions against Student data.
 * Supports: FIELD_NULL, FIELD_EQUALS, DATE_OVERDUE, STATUS_EQUALS
 */
@Service
public class TaskRuleEvaluator {

    /**
     * Returns true if the rule's condition is met for this student (task should be created).
     */
    public boolean shouldCreateTask(TaskRule rule, Student student) {
        try {
            String conditionType = rule.getConditionType();
            String field = rule.getConditionField();
            Object fieldValue = getFieldValue(student, field);

            switch (conditionType.toUpperCase()) {
                case "FIELD_NULL":
                    return fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty());

                case "FIELD_EQUALS":
                    if (fieldValue == null) return false;
                    return rule.getConditionValue().equalsIgnoreCase(fieldValue.toString());

                case "FIELD_NOT_NULL":
                    return fieldValue != null && !(fieldValue instanceof String && ((String) fieldValue).isEmpty());

                case "DATE_OVERDUE":
                    if (fieldValue == null) return false;
                    if (fieldValue instanceof LocalDate) {
                        int threshold = rule.getConditionDaysThreshold() != null ? rule.getConditionDaysThreshold() : 0;
                        return ((LocalDate) fieldValue).plusDays(threshold).isBefore(LocalDate.now());
                    }
                    return false;

                case "DATE_PASSED":
                    if (fieldValue == null) return false;
                    if (fieldValue instanceof LocalDate) {
                        return ((LocalDate) fieldValue).isBefore(LocalDate.now());
                    }
                    return false;

                case "STATUS_EQUALS":
                    if (student.getApplicationStatus() == null) return false;
                    return student.getApplicationStatus().name().equalsIgnoreCase(rule.getConditionValue());

                case "ACTION_EQUALS":
                    if (student.getAction() == null) return false;
                    return student.getAction().name().equalsIgnoreCase(rule.getConditionValue());

                case "COMPOUND_NULL_AND_NOT_NULL":
                    // conditionField = field that must be NULL
                    // conditionValue = field that must NOT be NULL (secondary check)
                    Object secondaryValue = getFieldValue(student, rule.getConditionValue());
                    boolean primaryNull = fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty());
                    boolean secondaryPresent = secondaryValue != null && !(secondaryValue instanceof String && ((String) secondaryValue).isEmpty());
                    return primaryNull && secondaryPresent;

                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Rule evaluation error for rule " + rule.getRuleCode() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns true if the auto-close condition is now satisfied (task should be closed).
     */
    public boolean shouldCloseTask(TaskRule rule, Student student) {
        try {
            String closeField = rule.getAutoCloseConditionField();
            if (closeField == null || closeField.isEmpty()) return false;

            Object fieldValue = getFieldValue(student, closeField);
            String closeValue = rule.getAutoCloseConditionValue();

            if (closeValue != null && !closeValue.isEmpty()) {
                // Equality check for auto-close
                return fieldValue != null && closeValue.equalsIgnoreCase(fieldValue.toString());
            } else {
                // Field not null means condition is satisfied
                return fieldValue != null && !(fieldValue instanceof String && ((String) fieldValue).isEmpty());
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Uses reflection to get a field value from the Student entity.
     */
    private Object getFieldValue(Student student, String fieldName) {
        if (fieldName == null || student == null) return null;

        try {
            // Build getter name: "admissionLetterPath" → "getAdmissionLetterPath"
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method method = student.getClass().getMethod(getterName);
            return method.invoke(student);
        } catch (NoSuchMethodException e) {
            // Try boolean getter: "isHasGivenNEET"
            try {
                String boolGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method method = student.getClass().getMethod(boolGetterName);
                return method.invoke(student);
            } catch (Exception ex) {
                System.err.println("Cannot resolve field: " + fieldName + " on Student");
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
