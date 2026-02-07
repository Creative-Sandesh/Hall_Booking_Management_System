package views;

import models.*;
import services.FileHandler;
import utils.IdGenerator;

import javax.swing.*;
import java.awt.*;

public class UserForm extends JFrame {

    private JComboBox<String> cmbRole;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private User userToEdit;
    private boolean isEditMode;

    public UserForm(User userToEdit) {
        this.userToEdit = userToEdit;
        this.isEditMode = (userToEdit != null);

        setTitle(isEditMode ? "Edit User" : "Add New User");
        setSize(400, 500); // Slightly taller to fit fields comfortably
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10)); // 7 Rows
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. Initialize Fields ---
        txtUsername = new JTextField(isEditMode ? userToEdit.getUsername() : "");
        txtPassword = new JPasswordField(isEditMode ? userToEdit.getPassword() : "");
        txtName = new JTextField(isEditMode ? userToEdit.getName() : "");
        txtEmail = new JTextField(isEditMode ? userToEdit.getEmail() : "");
        txtPhone = new JTextField();

        // --- 2. Setup Role Dropdown (Added MANAGER) ---
        String[] roles = {"CUSTOMER", "SCHEDULER", "MANAGER", "ADMIN"};
        cmbRole = new JComboBox<>(roles);

        // Handle Edit Mode Logic
        if (isEditMode) {
            cmbRole.setSelectedItem(userToEdit.getRole());
            cmbRole.setEnabled(false); // Disable role change during edit to prevent ID conflicts

            // If editing a Customer, load the phone number
            if (userToEdit instanceof Customer) {
                txtPhone.setText(((Customer) userToEdit).getContactNumber());
            }
        }

        // --- 3. Build UI ---
        panel.add(new JLabel("Username:"));     panel.add(txtUsername);
        panel.add(new JLabel("Password:"));     panel.add(txtPassword);
        panel.add(new JLabel("Full Name:"));    panel.add(txtName);
        panel.add(new JLabel("Email:"));        panel.add(txtEmail);
        panel.add(new JLabel("Role:"));         panel.add(cmbRole);
        panel.add(new JLabel("Phone (Cust Only):")); panel.add(txtPhone);

        JButton btnSave = new JButton(isEditMode ? "Update User" : "Create User");
        btnSave.setBackground(new Color(40, 167, 69)); // Green button
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> saveUser());

        add(panel, BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
    }

    private void saveUser() {
        String role = (String) cmbRole.getSelectedItem();
        String uName = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        // Basic Validation
        if(uName.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, Password, and Name are required!");
            return;
        }

        User newUser = null;
        // If editing, keep ID. If new, generate ID.
        String id = isEditMode ? userToEdit.getId() : IdGenerator.generateNextId("USER");

        // --- 4. Logic to Create Specific User Objects ---
        if (role.equals("CUSTOMER")) {
            newUser = new Customer(id, uName, pass, name, email, phone);
        }
        else if (role.equals("ADMIN")) {
            newUser = new Administrator(id, uName, pass, name, email);
        }
        else if (role.equals("SCHEDULER")) {
            newUser = new Scheduler(id, uName, pass, name, email);
        }
        else if (role.equals("MANAGER")) {
            // Ensure you have created the Manager.java model!
            newUser = new Manager(id, uName, pass, name, email);
        }

        // --- 5. Save to File ---
        if (newUser != null) {
            boolean success;
            if (isEditMode) {
                success = FileHandler.updateUser(newUser);
            } else {
                success = FileHandler.addUser(newUser);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, isEditMode ? "User Updated!" : "User Created!");
                dispose(); // Close form
            } else {
                JOptionPane.showMessageDialog(this, "Error saving user to file.");
            }
        }
    }
}