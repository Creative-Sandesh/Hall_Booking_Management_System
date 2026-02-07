package views;

import models.*;
import services.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ManagerDashboard extends BaseDashboard {

    public ManagerDashboard(Manager manager) {
        super("Manager Dashboard", manager, COLOR_MANAGER);

        // --- SIDEBAR ---
        addSidebarButton("Sales Dashboard", e -> showSalesDashboard());
        addSidebarButton("Manage Issues", e -> showIssuesView());
        addSidebarButton("Approve Bookings", e -> showApprovals());

        addLogoutButton();

        // Default Page
        showSalesDashboard();
        setVisible(true);
    }

    // =========================================================
    // FEATURE 1: SALES DASHBOARD (Weekly, Monthly, Yearly)
    // =========================================================
    private void showSalesDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("  Sales Performance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Filter Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        String[] periods = {"Weekly", "Monthly", "Yearly"};
        JComboBox<String> comboPeriod = new JComboBox<>(periods);
        JButton btnFilter = new JButton("Filter Report");

        topPanel.add(new JLabel("View By: "));
        topPanel.add(comboPeriod);
        topPanel.add(btnFilter);

        // Statistics Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        statsPanel.setBackground(Color.WHITE);

        // Initialize Labels
        JLabel lblTotalSales = createStatCard("Total Revenue", "RM 0.00");
        JLabel lblTotalBookings = createStatCard("Total Bookings", "0");
        JLabel lblAvgSale = createStatCard("Average Sale", "RM 0.00");

        statsPanel.add(lblTotalSales);
        statsPanel.add(lblTotalBookings);
        statsPanel.add(lblAvgSale);

        // Details Table
        String[] columns = {"Date", "Booking ID", "Hall", "Amount (RM)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // --- CALCULATION LOGIC ---
        Runnable calculateSales = () -> {
            model.setRowCount(0); // Clear table
            List<Booking> bookings = FileHandler.loadBookings();
            String period = (String) comboPeriod.getSelectedItem();
            LocalDate now = LocalDate.now();

            double totalRevenue = 0;
            int count = 0;

            for (Booking b : bookings) {
                // Only count APPROVED bookings as Sales
                if (!"APPROVED".equalsIgnoreCase(b.getStatus())) continue;

                boolean include = false;
                LocalDate d = b.getDate();

                if ("Weekly".equals(period)) {
                    WeekFields wf = WeekFields.of(Locale.getDefault());
                    int currentWeek = now.get(wf.weekOfWeekBasedYear());
                    int bookingWeek = d.get(wf.weekOfWeekBasedYear());
                    if (d.getYear() == now.getYear() && bookingWeek == currentWeek) {
                        include = true;
                    }
                } else if ("Monthly".equals(period)) {
                    if (d.getYear() == now.getYear() && d.getMonth() == now.getMonth()) {
                        include = true;
                    }
                } else if ("Yearly".equals(period)) {
                    if (d.getYear() == now.getYear()) {
                        include = true;
                    }
                }

                if (include) {
                    totalRevenue += b.getTotalPrice();
                    count++;
                    model.addRow(new Object[]{d.toString(), b.getBookingId(), b.getHallId(), String.format("%.2f", b.getTotalPrice())});
                }
            }

            // Update Labels
            lblTotalSales.setText(String.format("<html><center>Total Revenue<br/><font size=5 color=blue>RM %.2f</font></center></html>", totalRevenue));
            lblTotalBookings.setText(String.format("<html><center>Total Bookings<br/><font size=5 color=blue>%d</font></center></html>", count));

            double avg = count > 0 ? totalRevenue / count : 0;
            lblAvgSale.setText(String.format("<html><center>Average Sale<br/><font size=5 color=blue>RM %.2f</font></center></html>", avg));
        };

        // Initialize Data
        calculateSales.run();

        // Add Action Listener
        btnFilter.addActionListener(e -> calculateSales.run());

        // Layout Assembly
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(topPanel, BorderLayout.NORTH);
        centerContainer.add(statsPanel, BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(centerContainer, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(mainContent, BorderLayout.CENTER);

        setPage(panel);
    }

    private JLabel createStatCard(String title, String value) {
        JLabel l = new JLabel("<html><center>" + title + "<br/><font size=5 color=blue>" + value + "</font></center></html>", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        l.setPreferredSize(new Dimension(100, 80));
        return l;
    }

    // =========================================================
    // FEATURE 2: MANAGE ISSUES
    // =========================================================
    private void showIssuesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("  Maintenance Operations");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        List<Issue> issues = FileHandler.loadIssues();
        String[] columns = {"Issue ID", "Hall/Booking", "Assigned To", "Status", "Date"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Issue i : issues) {
            model.addRow(new Object[]{
                    i.getIssueId(),
                    i.getBookingId(),
                    i.getAssignedScheduler(),
                    i.getStatus(),
                    i.getDateReported().toString()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        // Double Click Listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String id = (String) model.getValueAt(row, 0);
                        Issue selected = issues.stream()
                                .filter(i -> i.getIssueId().equals(id))
                                .findFirst()
                                .orElse(null);

                        if (selected != null) {
                            showIssueDetailsDialog(selected, () -> showIssuesView()); // Pass refresh callback
                        }
                    }
                }
            }
        });

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JLabel("  (Double-click a row to Assign Scheduler or Update Status)"), BorderLayout.SOUTH);
        setPage(panel);
    }

    private void showIssueDetailsDialog(Issue issue, Runnable onSave) {
        JDialog dialog = new JDialog(this, "Manage Issue", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0;

        // Info Fields
        form.add(new JLabel("Issue ID: " + issue.getIssueId()), gbc); gbc.gridy++;
        form.add(new JLabel("Customer: " + issue.getCustomerEmail()), gbc); gbc.gridy++;

        gbc.gridy++;
        form.add(new JLabel("Description:"), gbc); gbc.gridy++;
        JTextArea desc = new JTextArea(issue.getDescription(), 3, 20);
        desc.setEditable(false);
        desc.setBackground(Color.LIGHT_GRAY);
        form.add(new JScrollPane(desc), gbc);

        // --- FIX: GET SCHEDULERS DIRECTLY (No FileHandler method needed) ---
        gbc.gridy++;
        form.add(new JLabel("Assign Scheduler:"), gbc); gbc.gridy++;

        List<User> users = FileHandler.loadUsers();
        List<String> schedulerNames = users.stream()
                .filter(u -> u instanceof models.Scheduler || u.getClass().getSimpleName().equalsIgnoreCase("Scheduler"))
                .map(User::getUsername)
                .collect(Collectors.toList());

        schedulerNames.add(0, "Unassigned"); // Add default
        JComboBox<String> schedulerBox = new JComboBox<>(schedulerNames.toArray(new String[0]));
        schedulerBox.setSelectedItem(issue.getAssignedScheduler());
        form.add(schedulerBox, gbc);

        // --- UPDATE STATUS ---
        gbc.gridy++;
        form.add(new JLabel("Update Status:"), gbc); gbc.gridy++;
        String[] statuses = {"In Progress", "Done", "Closed", "Cancelled"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        statusBox.setSelectedItem(issue.getStatus());
        form.add(statusBox, gbc);

        // Save Button
        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> {
            issue.setAssignedScheduler((String) schedulerBox.getSelectedItem());
            issue.setStatus((String) statusBox.getSelectedItem());

            if (FileHandler.updateIssue(issue)) {
                JOptionPane.showMessageDialog(dialog, "Issue Updated Successfully!");
                dialog.dispose();
                onSave.run(); // Refresh the parent table
            } else {
                JOptionPane.showMessageDialog(dialog, "Error saving.");
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =========================================================
    // FEATURE 3: APPROVE BOOKINGS
    // =========================================================
    private void showApprovals() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("  Approve Bookings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Customer", "Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);

        Runnable load = () -> {
            model.setRowCount(0);
            FileHandler.loadBookings().stream()
                    .filter(b -> b.getStatus().equals("PENDING"))
                    .forEach(b -> model.addRow(new Object[]{b.getBookingId(), b.getCustomerEmail(), b.getDate(), b.getStatus()}));
        };
        load.run();

        JButton btnApprove = new JButton("Approve Selected");
        btnApprove.setBackground(new Color(30, 58, 138));
        btnApprove.setForeground(Color.WHITE);

        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) model.getValueAt(row, 0);
                if(FileHandler.updateBookingStatus(id, "APPROVED")) {
                    load.run();
                    JOptionPane.showMessageDialog(this, "Booking Approved!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating booking.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a booking first.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnApprove);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        setPage(panel);
    }
}