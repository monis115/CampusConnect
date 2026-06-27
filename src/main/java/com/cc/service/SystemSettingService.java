package com.cc.service;

import com.cc.modals.SystemSetting;
import com.cc.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SystemSettingService {

    public static final String TASK_SYNC_INTERVAL_KEY = "TASK_SYNC_INTERVAL_MINUTES";
    public static final int DEFAULT_SYNC_INTERVAL_MINUTES = 15;

    @Autowired
    private SystemSettingRepository settingRepository;

    public int getTaskSyncIntervalMinutes() {
        Optional<SystemSetting> setting = settingRepository.findById(TASK_SYNC_INTERVAL_KEY);
        if (setting.isPresent()) {
            try {
                return Integer.parseInt(setting.get().getSettingValue());
            } catch (NumberFormatException e) {
                return DEFAULT_SYNC_INTERVAL_MINUTES;
            }
        }
        return DEFAULT_SYNC_INTERVAL_MINUTES;
    }

    public void setTaskSyncIntervalMinutes(int minutes) {
        if (minutes < 1) {
            minutes = 1; // Minimum interval
        }
        SystemSetting setting = new SystemSetting(TASK_SYNC_INTERVAL_KEY, String.valueOf(minutes));
        settingRepository.save(setting);
    }
}
