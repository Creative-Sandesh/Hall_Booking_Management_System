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


    // USER METHODS

    public static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        File file = new File(FileName.USERS.getFilename());

        if (!file.exists()) {
            return userList;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                // Basic validation: ID, User, Pass, Name, Email
                if (parts.length < 5) continue;

                String id = parts[0].trim();
                String username = parts[1].trim();
                String password = parts[2].trim();

                String name = parts[3].trim();
                String email = parts[4].trim();


                String role = parts[parts.length - 1].trim();

                if (role.equalsIgnoreCase("CUSTOMER")) {
                    // Customers have phone number at index 5, Role at index 6
                    String phone = (parts.length >= 6) ? parts[5].trim() : "-";
                    // If the role is actually at index 6, double check logic:
                    // ID,User,Pass,Name,Email,Phone,Role
                    userList.add(new Customer(id, username, password, name, email, phone));
                }
                else if (role.equalsIgnoreCase("ADMIN")) {
                    userList.add(new Administrator(id, username, password, name, email));
                }
                else if (role.equalsIgnoreCase("SCHEDULER")) {
                    userList.add(new Scheduler(id, username, password, name, email));
                }
                else if (role.equalsIgnoreCase("MANAGER")) {
                    userList.add(new Manager(id, username, password, name, email));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }
        return userList;
    }

    public static boolean addUser(User user) {
        return appendToFile(FileName.USERS.getFilename(), user.toFileString());
    }

    public static boolean updateUser(User updatedUser) {
        List<User> allUsers = loadUsers();
        boolean found = false;

        for (int i = 0; i < allUsers.size(); i++) {
            User existingUser = allUsers.get(i);
            if (existingUser.getId().equals(updatedUser.getId())) {
                // Check if email changed to update bookings
                if (!existingUser.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
                    updateBookingEmail(existingUser.getEmail(), updatedUser.getEmail());
                }
                allUsers.set(i, updatedUser);
                found = true;
                break;
            }
        }
        return found && rewriteUserFile(allUsers);
    }

    public static boolean deleteUser(String userId) {
        List<User> allUsers = loadUsers();
        boolean removed = allUsers.removeIf(u -> u.getId().equals(userId));
        return removed && rewriteUserFile(allUsers);
    }

    private static boolean rewriteUserFile(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName.USERS.getFilename()))) {
            for (User u : users) {
                writer.println(u.toFileString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    // BOOKING METHODS


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
                // If the file is old, it might not have remarks, so default to "None"
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
                        remarks // Pass Remarks
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
        return list;
    }


    public static boolean saveBooking(Booking booking) {
        return appendToFile(FileName.BOOKINGS.getFilename(), booking.toFileString());
    }

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

    // HALL METHODS


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
        return appendToFile(FileName.HALLS.getFilename(), hall.toFileString());
    }

    public static boolean deleteHall(String hallId) {
        List<Hall> allHalls = loadHalls();
        boolean removed = allHalls.removeIf(h -> h.getId().equals(hallId));
        return removed && saveAllHalls(allHalls);
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


    // ISSUE METHODS


    public static List<Issue> loadIssues() {
        List<Issue> list = new ArrayList<>();
        File file = new File(FileName.ISSUES.getFilename());
        if (!file.exists()) return list;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");

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
        return appendToFile(FileName.ISSUES.getFilename(), issue.toFileString());
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


    // SCHEDULE METHODS


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
        return appendToFile(FileName.SCHEDULES.getFilename(), schedule.toFileString());
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


    private static boolean appendToFile(String filename, String data) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(data + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to file " + filename + ": " + e.getMessage());
            return false;
        }
    }
}