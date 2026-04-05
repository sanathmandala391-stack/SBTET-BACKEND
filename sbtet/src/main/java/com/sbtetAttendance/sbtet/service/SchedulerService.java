package com.sbtetAttendance.sbtet.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    private final AttendanceService attendanceService;

    public SchedulerService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Every day at 11:59 PM
    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
    public void runEndOfDayJob() {
        try {
            attendanceService.markEndOfDayAttendance();
            System.out.println("End-of-day attendance processed.");
        } catch (Exception e) {
            System.err.println("Cron job error: " + e.getMessage());
        }
    }
}
