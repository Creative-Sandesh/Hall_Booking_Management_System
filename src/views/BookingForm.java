package views;

import models.*;
import services.BookingService;
import utils.IdGenerator;
import utils.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class BookingForm extends JFrame {

    private JTextField dateField;
    private JComboBox<String> startBox, endBox;
    private JTextArea remarksArea;
    private Customer customer;
    private Hall hall;

    public BookingForm(Customer customer, Hall hall) {
        this.customer = customer;
        this.hall = hall;

        setTitle("Book Hall: " + hall.getName());
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Hall: " + hall.getName()));
        panel.add(new JLabel("Rate: RM " + hall.getPricePerHour() + "/hr"));

        // --- DATE SECTION ---
        panel.add(new JLabel("Date:"));
        JPanel datePanel = new JPanel(new BorderLayout(5,0));
        dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setText(LocalDate.now().toString());

        JButton btnPickDate = new JButton("📅");
        btnPickDate.addActionListener(e -> new DatePicker(this, dateField).setVisible(true));

        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(btnPickDate, BorderLayout.EAST);
        panel.add(datePanel);

        // --- TIME SELECTION ---
        String[] timeSlots = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};

        panel.add(new JLabel("Start Time:"));
        startBox = new JComboBox<>(timeSlots);
        panel.add(startBox);

        panel.add(new JLabel("End Time:"));
        endBox = new JComboBox<>(timeSlots);
        endBox.setSelectedIndex(1);
        panel.add(endBox);

        // --- REMARKS UI ---
        panel.add(new JLabel("Remarks / Event Purpose:"));
        remarksArea = new JTextArea(3, 20);
        remarksArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        remarksArea.setLineWrap(true);

        JScrollPane scrollRemarks = new JScrollPane(remarksArea);
        panel.add(scrollRemarks);

        JButton btnConfirm = new JButton("Confirm Booking");
        btnConfirm.setBackground(new Color(30, 58, 138));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> attemptBooking());

        panel.add(btnConfirm);
        add(panel);
        setVisible(true);
    }

    private void attemptBooking() {
        try {
            // 1. Parse Inputs
            String dateString = dateField.getText();
            if(dateString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please pick a date.");
                return;
            }

            LocalDate date = LocalDate.parse(dateString);
            LocalTime start = LocalTime.parse((String) startBox.getSelectedItem());
            LocalTime end = LocalTime.parse((String) endBox.getSelectedItem());

            // 2. Validate Time basics (Service will also check this, but good to keep UI validation)
            if (!start.isBefore(end)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time!");
                return;
            }

            // 3. Clean Remarks (Remove commas to prevent CSV breakage)
            String rawRemarks = remarksArea.getText();
            String remarks = (rawRemarks == null || rawRemarks.trim().isEmpty())
                    ? "None"
                    : rawRemarks.replace(",", " ").trim();

            // 4. Calculate Price
            long duration = java.time.Duration.between(start, end).toHours();
            if (duration < 1) duration = 1; // Minimum 1 hour
            double total = duration * hall.getPricePerHour();

            // 5. Confirm Dialog
            int choice = JOptionPane.showConfirmDialog(this,
                    String.format("Total: RM %.2f\nEvent: %s\nProceed?", total, remarks),
                    "Confirm Booking", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                String newId = IdGenerator.generateNextId("BOOKING");

                Booking newBooking = new Booking(
                        newId,
                        customer.getEmail(),
                        hall.getId(),
                        date,
                        start,
                        end,
                        total,
                        "PENDING",
                        remarks
                );


                // 6. VALIDATE COLLISION AND SAVE

                String result = BookingService.createBooking(newBooking);

                if (result.equals("Success")) {
                    JOptionPane.showMessageDialog(this, "Booking Request Sent! Status: PENDING");
                    dispose(); // Close the form
                } else {
                    JOptionPane.showMessageDialog(this, result, "Booking Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}