package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private String bookingId;
    private String customerEmail;
    private String hallId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double totalPrice;
    private String status;
    private String remarks; // <--- NEW FIELD

    // Updated Constructor
    public Booking(String bookingId, String customerEmail, String hallId,
                   LocalDate date, LocalTime startTime, LocalTime endTime,
                   double totalPrice, String status, String remarks) {
        this.bookingId = bookingId;
        this.customerEmail = customerEmail;
        this.hallId = hallId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.status = status;
        this.remarks = (remarks == null || remarks.isEmpty()) ? "None" : remarks;
    }

    // --- GETTERS & SETTERS ---
    public String getBookingId() { return bookingId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String email) { this.customerEmail = email; }
    public String getHallId() { return hallId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // --- TO STRING (Updated for File Saving) ---
    public String toFileString() {
        return String.join(",",
                bookingId, customerEmail, hallId,
                date.toString(), startTime.toString(), endTime.toString(),
                String.format("%.2f", totalPrice),
                status, remarks // <--- Include remarks here!
        );
    }
}