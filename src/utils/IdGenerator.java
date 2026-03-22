package utils;

import enums.FileName;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class IdGenerator {


    private static int extractIdNumber(String idString) {
        if (idString == null) return 0;


        String numberPart = idString.replaceAll("\\D+", "");

        if (numberPart.isEmpty()) return 0;

        try {
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String generateNextId(String role) {
        String prefix = "U";
        String targetFilename = FileName.USERS.getFilename(); // Default to USERS file

        // Map role to both Prefix AND the correct File
        if (role.equalsIgnoreCase("CUSTOMER")) {
            prefix = "C";
        } else if (role.equalsIgnoreCase("ADMIN")) {
            prefix = "A";
        } else if (role.equalsIgnoreCase("SCHEDULER")) {
            prefix = "S";
        } else if (role.equalsIgnoreCase("MANAGER")) {
            prefix = "M";
        } else if (role.equalsIgnoreCase("ISSUE")) {
            prefix = "I";
            targetFilename = FileName.ISSUES.getFilename();
        } else if (role.equalsIgnoreCase("BOOKING")) {
            prefix = "B";
            targetFilename = FileName.BOOKINGS.getFilename();
        } else if (role.equalsIgnoreCase("SCH") || role.equalsIgnoreCase("SCHEDULE")) {
            prefix = "SCH";
            targetFilename = FileName.SCHEDULES.getFilename();
        }

        int maxId = 0;
        File file = new File(targetFilename);

        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(",");

                    if (parts.length > 0) {
                        String currentId = parts[0].trim();

                        if (currentId.startsWith(prefix)) {
                            int currentNum = extractIdNumber(currentId);
                            if (currentNum > maxId) {
                                maxId = currentNum;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error reading file for IDs: " + targetFilename);
            }
        }

        return String.format("%s%03d", prefix, maxId + 1);
    }
}