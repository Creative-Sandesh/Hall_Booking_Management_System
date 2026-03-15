package views;

import models.*;
import services.FileHandler;
import utils.IdGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulerDashboard extends BaseDashboard {

    private Scheduler currentScheduler;

    public SchedulerDashboard(Scheduler scheduler) {
        super("Scheduler Dashboard", scheduler, COLOR_SCHEDULER); // Teal Color
        this.currentScheduler = scheduler;

        // --- Sidebar Menu ---
        addSidebarButton("Booking Requests", e -> showBookingRequests());
        addSidebarButton("Maintenance Mgmt", e -> showMaintenanceView());
        addSidebarButton("Reported Issues", e -> showIssuesView());

        addLogoutButton();

        // --- Default View ---
        showBookingRequests();
        setVisible(true);
    }


    // VIEW 1: BOOKING REQUESTS (Approve/Reject)

    private void showBookingRequests() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("  Pending Booking Requests");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panel.add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking ID", "Hall", "Date", "Time", "Customer", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);

        // Load only PENDING bookings
        Runnable refresh = () -> {
            model.setRowCount(0);
            List<Booking> all = FileHandler.loadBookings();
            List<Booking> pending = all.stream()
                    .filter(b -> b.getStatus().equalsIgnoreCase("PENDING"))
                    .toList();

            for (Booking b : pending) {
                model.addRow(new Object[]{
                        b.getBookingId(), b.getHallId(), b.getDate(),
                        b.getStartTime() + "-" + b.getEndTime(),
                        b.getCustomerEmail(), b.getStatus()
                });
            }
        };
        refresh.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Actions
        JPanel footer = new JPanel();
        JButton btnApprove = new JButton("Approve");
        btnApprove.setBackground(new Color(40, 167, 69)); // Green
        btnApprove.setForeground(Color.WHITE);

        JButton btnReject = new JButton("Reject");
        btnReject.setBackground(new Color(220, 38, 38)); // Red
        btnReject.setForeground(Color.WHITE);

        // APPROVE LOGIC
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;
            String bid = (String) model.getValueAt(row, 0);

            if(FileHandler.updateBookingStatus(bid, "APPROVED")) {
                JOptionPane.showMessageDialog(this, "Booking Approved!");
                refresh.run();
            }
        });

        // REJECT LOGIC
        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;
            String bid = (String) model.getValueAt(row, 0);

            if(FileHandler.updateBookingStatus(bid, "REJECTED")) {
                JOptionPane.showMessageDialog(this, "Booking Rejected.");
                refresh.run();
            }
        });

        footer.add(btnApprove);
        footer.add(btnReject);
        panel.add(footer, BorderLayout.SOUTH);

        setPage(panel);
    }

    // VIEW 2: MAINTENANCE MANAGEMENT

    private void showMaintenanceView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new GridLayout(3, 2, 10, 10));
        header.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        header.setBackground(Color.WHITE);

        // Inputs to schedule maintenance
        JComboBox<String> cmbHalls = new JComboBox<>();
        FileHandler.loadHalls().forEach(h -> cmbHalls.addItem(h.getName() + " (" + h.getId() + ")"));

        JTextField txtDate = new JTextField("YYYY-MM-DD");
        JTextField txtStartTime = new JTextField("08:00");
        JTextField txtEndTime = new JTextField("18:00");

        header.add(new JLabel("Select Hall:")); header.add(cmbHalls);
        header.add(new JLabel("Date (YYYY-MM-DD):")); header.add(txtDate);
        header.add(new JLabel("Time Range:"));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.setBackground(Color.WHITE);
        txtStartTime.setPreferredSize(new Dimension(80, 25));
        txtEndTime.setPreferredSize(new Dimension(80, 25));
        timePanel.add(txtStartTime); timePanel.add(new JLabel(" to ")); timePanel.add(txtEndTime);
        header.add(timePanel);

        JButton btnSchedule = new JButton("Schedule Maintenance");
        btnSchedule.setBackground(COLOR_SCHEDULER);
        btnSchedule.setForeground(Color.WHITE);

        btnSchedule.addActionListener(e -> {
            try {
                String selectedStr = (String) cmbHalls.getSelectedItem();
                // Extract ID from "Name (ID)"
                String hallId = selectedStr.substring(selectedStr.lastIndexOf("(") + 1, selectedStr.lastIndexOf(")"));

                LocalDate date = LocalDate.parse(txtDate.getText());
                LocalTime start = LocalTime.parse(txtStartTime.getText());
                LocalTime end = LocalTime.parse(txtEndTime.getText());

                // Create Schedule Object
                String schedId = IdGenerator.generateNextId("SCH");
                HallSchedule maintenance = new HallSchedule(
                        schedId, hallId, "MAINTENANCE",
                        date, start, date, end, "Scheduled Repair"
                );

                if(FileHandler.saveSchedule(maintenance)) {
                    JOptionPane.showMessageDialog(this, "Maintenance Scheduled!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date/Time format.");
            }
        });

        panel.add(header, BorderLayout.NORTH);

        // Simple list of existing maintenance
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Hall", "Date", "Type"}, 0);
        JTable table = new JTable(model);
        List<HallSchedule> schedules = FileHandler.loadSchedules();
        for(HallSchedule s : schedules) {
            if(s.getType().equals("MAINTENANCE")) {
                model.addRow(new Object[]{s.getScheduleId(), s.getHallId(), s.getStartDate(), s.getType()});
            }
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnSchedule, BorderLayout.SOUTH);

        setPage(panel);
    }


    // VIEW 3: ISSUE RESOLUTION

    private void showIssuesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("  Reported Issues");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Issue ID", "Hall/Booking", "Description", "Status", "Assigned To"};
        DefaultTableModel model = new DefaultTableModel(cols, 0){
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);

        table.setRowHeight(30);

        Runnable refresh = () -> {
            model.setRowCount(0);
            List<Issue> issues = FileHandler.loadIssues();
            for(Issue i : issues) {
                model.addRow(new Object[]{
                        i.getIssueId(), i.getBookingId(), i.getDescription(), i.getStatus(), i.getAssignedScheduler()
                });
            }
        };
        refresh.run();

        JPanel footer = new JPanel();
        JButton btnResolve = new JButton("Mark Resolved");
        btnResolve.setBackground(new Color(40, 167, 69));
        btnResolve.setForeground(Color.WHITE);

        btnResolve.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;

            String issueId = (String) model.getValueAt(row, 0);

            // Find and Update
            List<Issue> issues = FileHandler.loadIssues();
            Issue target = issues.stream().filter(i -> i.getIssueId().equals(issueId)).findFirst().orElse(null);

            if(target != null) {
                target.setStatus("RESOLVED");
                target.setAssignedScheduler(currentScheduler.getName()); // Auto-assign to current user

                if(FileHandler.updateIssue(target)) {
                    JOptionPane.showMessageDialog(this, "Issue Resolved!");
                    refresh.run();
                }
            }
        });

        footer.add(btnResolve);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        setPage(panel);
    }
}