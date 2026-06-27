package com.cc.service.engine.generators;

import com.cc.modals.Student;
import org.springframework.stereotype.Component;

@Component
public class PaymentTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "FINANCE";
    }

    @Override
    protected boolean isStageActive(Student student) {
        // Operational Readiness: Accountant assigned & fee processing has started
        return student.getAccountant() != null && 
               (student.getVisaPaymentRequestedAmount() != null || student.getTelexPaymentAmount() != null);
    }
}
