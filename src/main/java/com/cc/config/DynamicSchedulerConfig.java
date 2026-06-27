package com.cc.config;

import com.cc.scheduler.TaskSyncService;
import com.cc.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class DynamicSchedulerConfig implements SchedulingConfigurer {

    @Autowired
    private TaskSyncService taskSyncService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                () -> taskSyncService.runScheduledSync(),
                triggerContext -> {
                    long intervalMs = systemSettingService.getTaskSyncIntervalMinutes() * 60000L;
                    Instant lastExecution = triggerContext.lastActualExecution();
                    
                    if (lastExecution == null) {
                        // First run 15 seconds after startup to let DataSeeder finish
                        return Instant.now().plusSeconds(15);
                    }
                    
                    // Subsequent runs based on the dynamic interval
                    return lastExecution.plusMillis(intervalMs);
                }
        );
    }
}
