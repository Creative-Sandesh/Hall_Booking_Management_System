package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class HallSchedule {
    private String scheduleId; // Matches getScheduleId()
    private String hallId;
    private String type; // "BOOKED" or "MAINTENANCE"
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String description;

    public HallSchedule(String scheduleId, String hallId, String type,
                        LocalDate startDate, LocalTime startTime,
                        LocalDate endDate, LocalTime endTime, String description) {
        this.scheduleId = scheduleId;
        this.hallId = hallId;
        this.type = type;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.description = description;
    }


    public String getScheduleId() { return scheduleId; }

    public String getHallId() { return hallId; }
    public String getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalDate getEndDate() { return endDate; }
    public LocalTime getEndTime() { return endTime; }
    public String getDescription() { return description; }

    public String toFileString() {
        return String.join(",",
                scheduleId, hallId, type,
                startDate.toString(), startTime.toString(),
                endDate.toString(), endTime.toString(),
                description
        );
    }
}