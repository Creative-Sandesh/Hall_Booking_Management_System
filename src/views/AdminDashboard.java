package views;

import models.*;
import services.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(User admin) {
        super("Administrator Dashboard", admin, COLOR_ADMIN);

        // --- SIDEBAR UPDATED ---
        addSidebarButton("Manage Managers", e -> showStaffManagement("MANAGER")); // NEW
        addSidebarButton("Manage Schedulers", e -> showStaffManagement("SCHEDULER"));
        addSidebarButton("Manage Customers", e -> showCustomerManagement());
        addSidebarButton("Booking Mgmt", e -> showBookingManagement());
        addSidebarButton("Manage Halls", e -> showHallManagement());

        addLogoutButton();

        // Default Page
        showStaffManagement("MANAGER");
        setVisible(true);
    }

    // ==========================================
    // VIEW 1: STAFF MANAGEMENT (Reused for Manager & Scheduler)
    // ==========================================
    private void showStaffManagement(String roleType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Dynamic Title (Manager or Scheduler)
        String titleText = roleType.substring(0, 1).toUpperCase() + roleType.substring(1).toLowerCase() + " Management";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JTextField txtSearch = new JTextField(15);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search Name/ID"));

        JButton btnAdd = new JButton("+ Add " + roleType.toLowerCase()); // Dynamic Button Text
        btnAdd.setBackground(COLOR_ADMIN);
        btnAdd.setForeground(Color.WHITE);

        actionPanel.add(txtSearch);
        actionPanel.add(btnAdd);

        header.add(title, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Username", "Name", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Load Data (Filter by passed roleType)
        refreshUserTable(model, roleType);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel();
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDel = new JButton("Delete Selected");
        btnDel.setBackground(Color.RED);
        btnDel.setForeground(Color.WHITE);

        footer.add(btnEdit);
        footer.add(btnDel);
        panel.add(footer, BorderLayout.SOUTH);

        // --- LISTENERS ---

        // Search
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Add
        btnAdd.addActionListener(e -> {
            new UserForm(null).setVisible(true);
            // Note: Ideally, pass the roleType to UserForm to auto-select "MANAGER" or "SCHEDULER"
        });

        // Edit
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) table.getValueAt(row, 0);
                User user = FileHandler.loadUsers().stream()
                        .filter(u -> u.getId().equals(id)).findFirst().orElse(null);
                if (user != null) new UserForm(user).setVisible(true);
            }
        });

        // Delete
        btnDel.addActionListener(e -> deleteUserAction(table, model));

        // Refresh
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshUserTable(model, roleType));
        header.add(btnRefresh, BorderLayout.CENTER);

        setPage(panel);
    }

    // ==========================================
    // VIEW 2: CUSTOMER MANAGEMENT (Kept Separate for extra fields)
    // ==========================================
    private void showCustomerManagement() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JTextField txtSearch = new JTextField(15);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search Customer"));
        header.add(title, BorderLayout.WEST);
        header.add(txtSearch, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Name", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        refreshUserTable(model, "CUSTOMER");
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton btnBlock = new JButton("Block / Delete User");
        btnBlock.setBackground(Color.RED);
        btnBlock.setForeground(Color.WHITE);
        footer.add(btnBlock);
        panel.add(footer, BorderLayout.SOUTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        btnBlock.addActionListener(e -> deleteUserAction(table, model));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshUserTable(model, "CUSTOMER"));
        header.add(btnRefresh, BorderLayout.CENTER);

        setPage(panel);
    }

    // Helper: Loads users into table based on Role
    private void refreshUserTable(DefaultTableModel model, String roleFilter) {
        model.setRowCount(0);
        List<User> users = FileHandler.loadUsers();
        for (User u : users) {
            // Check if user role matches the filter (MANAGER, SCHEDULER, CUSTOMER)
            if (u.getRole().equalsIgnoreCase(roleFilter)) {
                if (u instanceof Customer) {
                    model.addRow(new Object[]{u.getId(), u.getUsername(), u.getName(), u.getEmail(), ((Customer) u).getContactNumber()});
                } else {
                    model.addRow(new Object[]{u.getId(), u.getUsername(), u.getName(), u.getEmail(), u.getRole()});
                }
            }
        }
    }

    // Helper: Deletes user
    private void deleteUserAction(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }
        String id = (String) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (FileHandler.deleteUser(id)) {
                model.removeRow(row);
                JOptionPane.showMessageDialog(this, "User deleted.");
            }
        }
    }

    // ==========================================
    // VIEW 3: BOOKING & HALLS (Unchanged)
    // ==========================================
    private void showBookingManagement() {
        // ... (Same as previous Booking Management code) ...
        // Copy the 'showBookingManagement' method from the previous response here
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JLabel title = new JLabel("Booking Management");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        String[] filters = {"All Bookings", "Upcoming Bookings", "Past Bookings"};
        JComboBox<String> cmbFilter = new JComboBox<>(filters);
        JButton btnFilter = new JButton("Apply Filter");
        header.add(title); header.add(new JLabel("| View:")); header.add(cmbFilter); header.add(btnFilter);
        panel.add(header, BorderLayout.NORTH);
        String[] cols = {"Booking ID", "Customer", "Hall", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        Runnable loadData = () -> {
            model.setRowCount(0);
            List<Booking> all = FileHandler.loadBookings();
            String filter = (String) cmbFilter.getSelectedItem();
            LocalDate today = LocalDate.now();
            for (Booking b : all) {
                boolean show = false;
                if (filter.equals("All Bookings")) show = true;
                else if (filter.equals("Upcoming Bookings") && !b.getDate().isBefore(today)) show = true;
                else if (filter.equals("Past Bookings") && b.getDate().isBefore(today)) show = true;
                if (show) model.addRow(new Object[]{b.getBookingId(), b.getCustomerEmail(), b.getHallId(), b.getDate(), b.getStartTime(), b.getStatus()});
            }
        };
        loadData.run();
        btnFilter.addActionListener(e -> loadData.run());
        setPage(panel);
    }

    private void showHallManagement() {
        // ... (Same as previous Hall Management code) ...
        // Copy the 'showHallManagement' method from the previous response here
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  Manage Halls");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JButton btnAdd = new JButton("+ Add New Hall");
        btnAdd.setBackground(COLOR_SCHEDULER);
        btnAdd.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(header, BorderLayout.NORTH);
        String[] cols = {"ID", "Name", "Type", "Capacity", "Rate (RM)", "Maintenance"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        model.setRowCount(0);
        for(Hall h : FileHandler.loadHalls()) model.addRow(new Object[]{h.getId(), h.getName(), h.getType(), h.getCapacity(), h.getPricePerHour(), h.isMaintenance()});
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel footer = new JPanel();
        JButton btnDel = new JButton("Delete Hall");
        btnDel.setBackground(Color.RED); btnDel.setForeground(Color.WHITE);
        footer.add(btnDel);
        panel.add(footer, BorderLayout.SOUTH);
        btnAdd.addActionListener(e -> new HallForm().setVisible(true));
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                if(FileHandler.deleteHall((String) model.getValueAt(row, 0))) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Hall deleted.");
                }
            }
        });
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            model.setRowCount(0);
            for(Hall h : FileHandler.loadHalls()) model.addRow(new Object[]{h.getId(), h.getName(), h.getType(), h.getCapacity(), h.getPricePerHour(), h.isMaintenance()});
        });
        header.add(btnRefresh, BorderLayout.CENTER);
        setPage(panel);
    }
}