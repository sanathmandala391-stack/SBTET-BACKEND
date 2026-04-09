
//package com.sbtetAttendance.sbtet.service;


//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;

//@Service
//public class SchedulerService {

//private final AttendanceService attendanceService;

//    public SchedulerService(AttendanceService attendanceService) {
        //this.attendanceService = attendanceService;
    //}

    // Every day at 11:59 PM
   // @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
   // public void runEndOfDayJob() {
       // try {
            //attendanceService.markEndOfDayAttendance();
      //      System.out.println("End-of-day attendance processed.");
     //   }// catch (Exception e) {
        // System.err.println("Cron job error: " + e.getMessage());
//   }
   // }
//}




//Second code//

package com.sbtetAttendance.sbtet.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
@Service
public class SchedulerService {

    private final AttendanceService attendanceService;

    public SchedulerService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Every day at 11:59 PM — mark E for no-checkout, mark A for no-show
    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
    public void runEndOfDayJob() {
        try {
            attendanceService.markEndOfDayAttendance();
            System.out.println("End-of-day attendance processed.");
        } catch (Exception e) {
            System.err.println("End-of-day cron error: " + e.getMessage());
        }
    }

    // Every day at 5:00 AM — recalculate all summaries (percentage update)
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Kolkata")
    public void runMorningRecalculation() {
        try {
            attendanceService.recalculateAllSummaries();
            System.out.println("Morning recalculation done at 5:00 AM.");
        } catch (Exception e) {
            System.err.println("Morning cron error: " + e.getMessage());
        }
    }
            @EventListener(ApplicationReadyEvent.class)
    public void fixMissingPastAttendance() {
        try {
            attendanceService.fixAllMissingPastDates();
            System.out.println("Past attendance fix completed on startup.");
        } catch (Exception e) {
            System.err.println("Past fix error: " + e.getMessage());
        }
    }
}
