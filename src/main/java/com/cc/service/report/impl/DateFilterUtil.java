package com.cc.service.report.impl;

import com.cc.DTO.report.ReportRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DateFilterUtil {
    public static <T> List<T> filter(List<T> data, ReportRequest request, Function<T, LocalDateTime> dateExtractor) {
        if (data == null || data.isEmpty()) return data;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(request.getStartDate()).atStartOfDay();
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(request.getEndDate()).atTime(LocalTime.MAX);
        }

        if (startDate == null && endDate == null) {
            return data;
        }

        final LocalDateTime finalStart = startDate;
        final LocalDateTime finalEnd = endDate;

        return data.stream().filter(item -> {
            LocalDateTime date = dateExtractor.apply(item);
            if (date == null) return true; // keep items without dates, or maybe false? Keep true for safety.
            if (finalStart != null && date.isBefore(finalStart)) return false;
            if (finalEnd != null && date.isAfter(finalEnd)) return false;
            return true;
        }).collect(Collectors.toList());
    }
}
