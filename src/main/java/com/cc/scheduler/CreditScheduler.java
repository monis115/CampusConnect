package com.cc.scheduler;

import com.cc.modals.CreditInstallment;
import com.cc.repository.CreditInstallmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CreditScheduler {

    @Autowired
    private CreditInstallmentRepository creditInstallmentRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs every midnight
    public void markOverdueInstallments() {
        List<CreditInstallment> pendingInstallments = creditInstallmentRepository.findByStatus("PENDING");
        LocalDate today = LocalDate.now();

        for (CreditInstallment inst : pendingInstallments) {
            if (inst.getDueDate() != null && inst.getDueDate().isBefore(today)) {
                inst.setStatus("OVERDUE");
                creditInstallmentRepository.save(inst);
            }
        }
    }
}
