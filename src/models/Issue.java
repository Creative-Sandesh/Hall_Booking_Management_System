package models;

import java.time.LocalDate;

public class Issue {
    private String issueId;
    private String bookingId;
    private String customerEmail;
    private String description;
    private String status;
    private LocalDate dateReported;
    private String assignedScheduler; // <--- NEW FIELD

    public Issue(String issueId, String bookingId, String customerEmail, String description, String status, LocalDate dateReported, String assignedScheduler) {
        this.issueId = issueId;
        this.bookingId = bookingId;
        this.customerEmail = customerEmail;
        this.description = description;
        this.status = status;
        this.dateReported = dateReported;
        this.assignedScheduler = assignedScheduler;
    }

    // Getters and Setters
    public String getIssueId() { return issueId; }
    public String getBookingId() { return bookingId; }
    public String getCustomerEmail() { return customerEmail; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDateReported() { return dateReported; }

    public String getAssignedScheduler() { return assignedScheduler; }
    public void setAssignedScheduler(String assignedScheduler) { this.assignedScheduler = assignedScheduler; }

    public String toFileString() {
        // Now saves 7 items instead of 6
        return String.join(",", issueId, bookingId, customerEmail, description, status, dateReported.toString(), assignedScheduler);
    }
}