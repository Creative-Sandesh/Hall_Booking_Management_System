package services;

import enums.FileName;
import models.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileHandler {

    // --- USER METHODS ---

    public static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        File file = new File(FileName.USERS.getFilename());

        if (!file.exists()) {
            try {
                if(file.getParentFile() != null) file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Could not create user file: " + e.getMessage());
            }
            return userList;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) continue; // Basic validation

                String id = parts[0].trim();
                String username = parts[1].trim();
                String password = parts[2].trim();
                String name = parts[3].trim();
                String email = parts[4].trim();
                String role = parts[parts.length - 1].trim();

                if (role.equalsIgnoreCase("CUSTOMER")) {
                    if (parts.length >= 7) {
                        String phone = parts[5].trim();
                        userList.add(new Customer(id, username, password, name, email, phone));
                    }
                } else if (role.equalsIgnoreCase("ADMIN")) {
                    userList.add(new Administrator(id, username, password, name, email));
                } else if (role.equalsIgnoreCase("SCHEDULER")) {
                    userList.add(new Scheduler(id, username, password, name, email));
                } else if (role.equalsIgnoreCase("MANAGER")) {
                    userList.add(new Manager(id, username, password, name, email));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }
        return userList;
    }

    public static boolean addUser(User user) {
        try (FileWriter fw = new FileWriter(FileName.USERS.getFilename(), true)) {
            fw.write(user.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUser(User updatedUser) {
        List<User> allUsers = loadUsers();
        boolean found = false;

        for (int i = 0; i < allUsers.size(); i++) {
            User existingUser = allUsers.get(i);
            if (existingUser.getId().equals(updatedUser.getId())) {

                // Check if email changed to update bookings
                String oldEmail = existingUser.getEmail();
                String newEmail = updatedUser.getEmail();
                if (!oldEmail.equalsIgnoreCase(newEmail)) {
                    updateBookingEmail(oldEmail, newEmail);
                }

                allUsers.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.USERS.getFilename()))) {
            for (User u : allUsers) {
                writer.println(u.toFileString());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error updating users: " + e.getMessage());
            return false;
        }
    }

    // --- HALL METHODS ---

    public static List<Hall> loadHalls() {
        List<Hall> hallList = new ArrayList<>();
        File file = new File(FileName.HALLS.getFilename());

        if (!file.exists()) return hallList;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                hallList.add(new Hall(
                        parts[0],
                        parts[1],
                        Double.parseDouble(parts[2]),
                        Integer.parseInt(parts[3]),
                        Boolean.parseBoolean(parts[4])
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading halls: " + e.getMessage());
        }
        return hallList;
    }

    public static boolean addHall(Hall hall) {
        try (FileWriter fw = new FileWriter(FileName.HALLS.getFilename(), true)) {
            fw.write(hall.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean saveAllHalls(List<Hall> halls) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.HALLS.getFilename()))) {
            for (Hall h : halls) {
                writer.println(h.toFileString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // --- BOOKING METHODS ---

    public static List<Booking> loadBookings() {
        List<Booking> list = new ArrayList<>();
        File file = new File(FileName.BOOKINGS.getFilename());
        if (!file.exists()) return list;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");

                // We need at least 8 columns.
                if (p.length < 8) continue;

                // Check if remarks exist (Column index 8)
                String remarks = (p.length >= 9) ? p[8] : "None";

                list.add(new Booking(
                        p[0], // ID
                        p[1], // Email
                        p[2], // HallID
                        LocalDate.parse(p[3]), // Date
                        LocalTime.parse(p[4]), // Start
                        LocalTime.parse(p[5]), // End
                        Double.parseDouble(p[6]), // Price
                        p[7], // Status
                        remarks // <--- PASS REMARKS TO CONSTRUCTOR
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveBooking(Booking booking) {
        try (FileWriter fw = new FileWriter(FileName.BOOKINGS.getFilename(), true)) {
            fw.write(booking.toFileString() + "\n"); // Uses the new 9-column format automatically
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Critical method for cancelling bookings
    public static boolean updateBookingStatus(String bookingId, String newStatus) {
        List<Booking> allBookings = loadBookings();
        boolean found = false;

        for (Booking b : allBookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus(newStatus);
                found = true;
                break;
            }
        }
        return found && saveAllBookings(allBookings);
    }

    // Critical method for overwriting booking file
    public static boolean saveAllBookings(List<Booking> bookings) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.BOOKINGS.getFilename()))) {
            for (Booking b : bookings) {
                writer.println(b.toFileString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void updateBookingEmail(String oldEmail, String newEmail) {
        List<Booking> allBookings = loadBookings();
        boolean changed = false;
        for (Booking b : allBookings) {
            if (b.getCustomerEmail().equalsIgnoreCase(oldEmail)) {
                b.setCustomerEmail(newEmail);
                changed = true;
            }
        }
        if (changed) saveAllBookings(allBookings);
    }

    // --- ISSUE METHODS ---

    public static List<Issue> loadIssues() {
        List<Issue> list = new ArrayList<>();
        File file = new File(FileName.ISSUES.getFilename());
        if (!file.exists()) return list;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");

                // Ensure we have at least the basic fields (Index 0 to 5)
                if (p.length >= 6) {
                    String scheduler = (p.length >= 7) ? p[6] : "Unassigned";

                    list.add(new Issue(
                            p[0], p[1], p[2], p[3], p[4],
                            LocalDate.parse(p[5]),
                            scheduler
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading issues: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveIssue(Issue issue) {
        try (FileWriter fw = new FileWriter(FileName.ISSUES.getFilename(), true)) {
            fw.write(issue.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateIssue(Issue updatedIssue) {
        List<Issue> allIssues = loadIssues();
        boolean found = false;

        for (int i = 0; i < allIssues.size(); i++) {
            if (allIssues.get(i).getIssueId().equals(updatedIssue.getIssueId())) {
                allIssues.set(i, updatedIssue);
                found = true;
                break;
            }
        }

        if (!found) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.ISSUES.getFilename()))) {
            for (Issue issue : allIssues) {
                writer.println(issue.toFileString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // --- SCHEDULE METHODS ---

    public static List<HallSchedule> loadSchedules() {
        List<HallSchedule> list = new ArrayList<>();
        File file = new File(FileName.SCHEDULES.getFilename());
        if (!file.exists()) return list;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 8) {
                    list.add(new HallSchedule(
                            p[0], p[1], p[2],
                            LocalDate.parse(p[3]), LocalTime.parse(p[4]),
                            LocalDate.parse(p[5]), LocalTime.parse(p[6]),
                            p[7]
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading schedules");
        }
        return list;
    }

    public static boolean saveSchedule(HallSchedule schedule) {
        try (FileWriter fw = new FileWriter(FileName.SCHEDULES.getFilename(), true)) {
            fw.write(schedule.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean saveAllSchedules(List<HallSchedule> schedules) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.SCHEDULES.getFilename()))) {
            for (HallSchedule s : schedules) {
                writer.println(s.toFileString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ==========================================
    // SAVE A NEW BOOKING (Updated with Remarks)
    // ==========================================
    public static boolean saveBooking(Booking booking, String remarks) {
        // Format: ID,UserEmail,HallID,Date,StartTime,EndTime,TotalAmount,Status,Remarks
        String record = String.format("%s,%s,%s,%s,%s,%s,%.2f,%s,%s",
                booking.getBookingId(),
                booking.getCustomerEmail(),
                booking.getHallId(),
                booking.getDate().toString(),
                booking.getStartTime().toString(),
                booking.getEndTime().toString(),
                booking.getTotalPrice(),
                booking.getStatus(),
                remarks // <--- Added Remarks at the end
        );

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter("data/bookings.txt", true))) {
            writer.write(record);
            writer.newLine();
            return true;
        } catch (java.io.IOException e) {
            System.out.println("Error saving booking: " + e.getMessage());
            return false;
        }
    }
    // --- NEW: DELETE USER ---
    public static boolean deleteUser(String userId) {
        List<User> allUsers = loadUsers();
        boolean removed = allUsers.removeIf(u -> u.getId().equals(userId));

        if (removed) {
            // Rewrite the file without the deleted user
            try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.USERS.getFilename()))) {
                for (User u : allUsers) {
                    writer.println(u.toFileString());
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    // --- NEW: DELETE HALL ---
    public static boolean deleteHall(String hallId) {
        List<Hall> allHalls = loadHalls();
        boolean removed = allHalls.removeIf(h -> h.getId().equals(hallId));

        if (removed) {
            return saveAllHalls(allHalls); // Reuse your existing saveAllHalls method
        }
        return false;
    }






}