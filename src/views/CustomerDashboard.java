package views;

import models.*;
import services.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerDashboard extends BaseDashboard {

    private Customer customer;

    public CustomerDashboard(Customer customer) {
        // 1. Call your BaseDashboard constructor
        // We use your static color: COLOR_PRIMARY (Blue)
        super("Customer Dashboard", customer, COLOR_PRIMARY);
        this.customer = customer;

        // 2. Add Sidebar Buttons using your helper method
        addSidebarButton("View Available Halls", e -> showHallsView());
        addSidebarButton("My Bookings", e -> showBookingsView());
        addSidebarButton("My Issues", e -> showIssuesView());
        addSidebarButton("My Profile", e -> showProfileView());

        // 3. Add Logout (Your BaseDashboard handles the logic)
        addLogoutButton();

        // 4. Show default view
        showHallsView();
        setVisible(true);
    }

    // VIEW 1: AVAILABLE HALLS

    private void showHallsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("  Available Halls");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Load Data
        List<Hall> halls = FileHandler.loadHalls();
        List<HallSchedule> schedules = FileHandler.loadSchedules();
        LocalDate today = LocalDate.now();

        String[] columns = {"ID", "Hall Name", "Rate (RM/hr)", "Capacity", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Hall h : halls) {
            String status = "Available";
            if (h.isMaintenance()) status = "Under Maintenance";

            // Check Schedule conflicts
            if (status.equals("Available")) {
                for (HallSchedule s : schedules) {
                    if (s.getHallId().equals(h.getId())) {
                        boolean isTodayInSchedule = (today.isEqual(s.getStartDate()) || today.isAfter(s.getStartDate())) &&
                                (today.isEqual(s.getEndDate()) || today.isBefore(s.getEndDate()));
                        if (isTodayInSchedule) {
                            if (s.getType().equalsIgnoreCase("MAINTENANCE")) status = "Under Maintenance";
                            else if (s.getType().equalsIgnoreCase("BOOKED")) status = "Booked Today";
                        }
                    }
                }
            }
            model.addRow(new Object[]{h.getId(), h.getName(), String.format("%.2f", h.getPricePerHour()), h.getCapacity(), status});
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        // Booking Action
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBook = new JButton("Book Selected Hall");
        btnBook.setBackground(new Color(40, 167, 69)); // Green
        btnBook.setForeground(Color.WHITE);

        btnBook.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a hall.");
                return;
            }

            String currentStatus = (String) model.getValueAt(row, 4);
            if (!currentStatus.equals("Available")) {
                JOptionPane.showMessageDialog(this, "Cannot book: " + currentStatus);
                return;
            }

            String hallId = (String) model.getValueAt(row, 0);

            // Find the Hall object
            Hall selectedHall = halls.stream()
                    .filter(h -> h.getId().equals(hallId))
                    .findFirst()
                    .orElse(null);

            if (selectedHall != null) {
                new BookingForm(customer, selectedHall).setVisible(true);
            }
        });

        bottomPanel.add(btnBook);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Call your helper method
        setPage(panel);
    }


    // VIEW 2: MY BOOKINGS

    private void showBookingsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] filters = {"All Bookings", "Upcoming", "Past History"};
        JComboBox<String> filterBox = new JComboBox<>(filters);
        JLabel lblHeader = new JLabel("My Booking History");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));

        topBar.add(lblHeader, BorderLayout.WEST);
        topBar.add(filterBox, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Hall Name", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);

        // Function to refresh table data
        Runnable refreshTable = () -> {
            model.setRowCount(0);
            String filter = (String) filterBox.getSelectedItem();
            LocalDate today = LocalDate.now();
            List<Booking> all = FileHandler.loadBookings();
            List<Hall> allHalls = FileHandler.loadHalls();

            List<Booking> userBookings = all.stream()
                    .filter(b -> b.getCustomerEmail().equalsIgnoreCase(customer.getEmail()))
                    .filter(b -> {
                        if ("Upcoming".equals(filter)) return !b.getDate().isBefore(today);
                        if ("Past History".equals(filter)) return b.getDate().isBefore(today);
                        return true;
                    }).collect(Collectors.toList());

            for (Booking b : userBookings) {
                String hallName = allHalls.stream()
                        .filter(h -> h.getId().equals(b.getHallId()))
                        .map(Hall::getName)
                        .findFirst()
                        .orElse(b.getHallId());
                model.addRow(new Object[]{b.getBookingId(), hallName, b.getDate(), b.getStartTime() + "-" + b.getEndTime(), b.getStatus()});
            }
        };

        refreshTable.run(); // Initial Load
        filterBox.addActionListener(e -> refreshTable.run());

        // Actions
        JPanel bottomPanel = new JPanel();

        JButton btnIssue = new JButton("Report Issue");
        btnIssue.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }

            String bid = (String) model.getValueAt(row, 0);
            Booking b = FileHandler.loadBookings().stream().filter(bk->bk.getBookingId().equals(bid)).findFirst().orElse(null);

            if(b != null) new IssueForm(customer, b).setVisible(true);
        });

        JButton btnCancel = new JButton("Cancel Booking");
        btnCancel.setBackground(Color.RED);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String bid = (String) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                if (FileHandler.updateBookingStatus(bid, "CANCELLED")) {
                    JOptionPane.showMessageDialog(this, "Booking Cancelled!");
                    refreshTable.run();
                }
            }
        });

        bottomPanel.add(btnIssue);
        bottomPanel.add(btnCancel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        setPage(panel);
    }


    // VIEW 3: MY ISSUES

    private void showIssuesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("  Reported Issues Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        String[] columns = {"Issue ID", "Booking ID", "Description", "Status", "Date Reported"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        List<Issue> allIssues = FileHandler.loadIssues();
        List<Issue> myIssues = allIssues.stream()
                .filter(i -> i.getCustomerEmail().equalsIgnoreCase(customer.getEmail()))
                .collect(Collectors.toList());

        for (Issue i : myIssues) {
            model.addRow(new Object[]{
                    i.getIssueId(), i.getBookingId(), i.getDescription(), i.getStatus(), i.getDateReported().toString()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        setPage(panel);
    }


    // VIEW 4: PROFILE

    private void showProfileView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; panel.add(title, g);
        g.gridwidth = 1;

        g.gridy++; g.gridx = 0; panel.add(new JLabel("Full Name:"), g);
        g.gridx = 1; JTextField txtName = new JTextField(customer.getName(), 20); txtName.setEditable(false); panel.add(txtName, g);

        g.gridy++; g.gridx = 0; panel.add(new JLabel("Email:"), g);
        g.gridx = 1; JTextField txtEmail = new JTextField(customer.getEmail(), 20); txtEmail.setEditable(false); panel.add(txtEmail, g);

        g.gridy++; g.gridx = 0; panel.add(new JLabel("Phone:"), g);
        g.gridx = 1; JTextField txtPhone = new JTextField(customer.getContactNumber(), 20); txtPhone.setEditable(false); panel.add(txtPhone, g);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnEdit = new JButton("Edit Profile");
        JButton btnSave = new JButton("Save Changes");
        btnSave.setEnabled(false);
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);

        btnEdit.addActionListener(e -> {
            boolean isEditing = btnSave.isEnabled();
            txtName.setEditable(!isEditing);
            // txtEmail.setEditable(!isEditing); // Email is usually ID, best not to edit
            txtPhone.setEditable(!isEditing);
            btnSave.setEnabled(!isEditing);
            btnEdit.setText(isEditing ? "Edit Profile" : "Cancel");

            if(isEditing) {
                // Revert to original if cancelling
                txtName.setText(customer.getName());
                txtEmail.setText(customer.getEmail());
                txtPhone.setText(customer.getContactNumber());
            }
        });

        btnSave.addActionListener(e -> {
            customer.setName(txtName.getText());
            // customer.setEmail(txtEmail.getText());
            customer.setContactNumber(txtPhone.getText());

            if(FileHandler.updateUser(customer)) {
                JOptionPane.showMessageDialog(this, "Profile Saved!");
                txtName.setEditable(false); txtEmail.setEditable(false); txtPhone.setEditable(false);
                btnSave.setEnabled(false);
                btnEdit.setText("Edit Profile");
            } else {
                JOptionPane.showMessageDialog(this, "Error saving profile.");
            }
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnSave);

        g.gridy++; g.gridx = 0; g.gridwidth = 2; panel.add(btnPanel, g);

        JPanel wrapper = new JPanel(new FlowLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(panel);
        setPage(wrapper);
    }
}