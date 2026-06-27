package com.cc.service.engine.generators;

import org.springframework.stereotype.Component;

@Component
public class AdmissionTaskGenerator extends BaseTaskGenerator {
    @Override
    public String getDepartment() {
        return "ADMISSION";
    }
}
