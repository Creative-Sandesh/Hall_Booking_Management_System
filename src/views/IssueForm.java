package views;

import models.Booking;
import models.Customer;
import models.Issue;
import services.FileHandler;
import utils.IdGenerator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class IssueForm extends JFrame {

    public IssueForm(Customer customer, Booking booking) {
        setTitle("Report an Issue");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel lblHeader = new JLabel("Report Issue for Booking: " + booking.getBookingId());
        lblHeader.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblHeader, BorderLayout.NORTH);

        // Text Area for Description
        JTextArea txtDescription = new JTextArea();
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane scroll = new JScrollPane(txtDescription);
        scroll.setBorder(BorderFactory.createTitledBorder("Describe your issue:"));
        panel.add(scroll, BorderLayout.CENTER);

        // Submit Button
        JButton btnSubmit = new JButton("Submit Report");
        btnSubmit.setBackground(new Color(220, 53, 69)); // Red color for issues
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 13));

        btnSubmit.addActionListener(e -> {
            String desc = txtDescription.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please describe the issue.");
                return;
            }

            // Create Issue Object
            String issueId = IdGenerator.generateNextId("ISSUE");


            Issue newIssue = new Issue(
                    issueId,
                    booking.getBookingId(),
                    customer.getEmail(),
                    desc,
                    "OPEN",
                    LocalDate.now(),
                    "Unassigned"
            );

            if (FileHandler.saveIssue(newIssue)) {
                JOptionPane.showMessageDialog(this, "Issue Reported Successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving issue.");
            }
        });

        panel.add(btnSubmit, BorderLayout.SOUTH);
        add(panel);
        setVisible(true);
    }
}