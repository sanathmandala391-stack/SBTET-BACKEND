package com.sbtetAttendance.sbtet.util;
import com.sbtetAttendance.sbtet.model.College;
import com.sbtetAttendance.sbtet.model.CollegeHoliday;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class GeofenceUtil {

    /** Haversine formula - returns distance in meters */
    public double getDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public boolean isWithinCollege(double lat, double lon, College college) {
        double distance = getDistanceMeters(lat, lon, college.getLatitude(), college.getLongitude());
        return distance <= college.getGeofenceRadius();
    }

    public int getDistanceFromCollege(double lat, double lon, College college) {
        return (int) Math.round(getDistanceMeters(lat, lon, college.getLatitude(), college.getLongitude()));
    }

    /**
     * Telangana SBTET rules:
     *  - All Sundays are off
     *  - 2nd Saturday of each month is off
     *  - 1st, 3rd, 4th Saturdays are working
     */
    public boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SUNDAY) return true;
        if (dow == DayOfWeek.SATURDAY) {
            int dayOfMonth = date.getDayOfMonth();
            return dayOfMonth > 7 && dayOfMonth <= 14; // 2nd Saturday
        }
        return false;
    }

    public boolean isHoliday(LocalDate date, List<CollegeHoliday> holidays) {
        return holidays.stream().anyMatch(h -> h.getDate().equals(date));
    }
}
