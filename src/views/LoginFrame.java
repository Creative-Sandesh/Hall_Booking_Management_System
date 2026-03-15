package views;

import models.*;
import models.User;
import services.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // PALETTE COLORS
    private final Color PRIMARY_COLOR = new Color(30, 58, 138); // #1E3A8A
    private final Color NEUTRAL_LIGHT = new Color(243, 244, 246); // #F3F4F6
    private final Color NEUTRAL_DARK = new Color(55, 65, 81); // #374151

    public LoginFrame() {
        setTitle("Hall Booking Management System - Login");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainPanel.setBackground(NEUTRAL_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 1. Title Label
        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(NEUTRAL_DARK);
        mainPanel.add(titleLabel);

        // 2. Username
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(NEUTRAL_LIGHT);
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(NEUTRAL_DARK);
        usernameField = new JTextField();
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(usernameField, BorderLayout.CENTER);
        mainPanel.add(userPanel);

        // 3. Password
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(NEUTRAL_LIGHT);
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(NEUTRAL_DARK);
        passwordField = new JPasswordField();
        passPanel.add(passLabel, BorderLayout.NORTH);
        passPanel.add(passwordField, BorderLayout.CENTER);
        mainPanel.add(passPanel);

        // 4. Login Button
        loginButton = new JButton("Login");
        styleButton(loginButton); // Apply custom style
        mainPanel.add(loginButton);

        // 5. REGISTER BUTTON ---
        JButton registerButton = new JButton("Don't have an account? Sign Up");
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false); // Make it look like a link
        registerButton.setForeground(PRIMARY_COLOR);
        registerButton.setFont(new Font("Arial", Font.BOLD, 12));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(registerButton);

        // Login Logic
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // register logic
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // Close Login Window
                new RegisterFrame(); // Open Registration Window
            }
        });

        add(mainPanel);
        setVisible(true);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }



    private void performLogin() {
        String inputUser = usernameField.getText();
        String inputPass = new String(passwordField.getPassword());

        // Call the Service to handle logic
        User user = services.AuthService.login(inputUser, inputPass);

        if (user != null) {
            // Success! Route to correct dashboard
            if (user.getRole().equalsIgnoreCase("CUSTOMER")) {
                new CustomerDashboard((models.Customer) user);
            } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
                new AdminDashboard((models.Administrator) user);
            } else if (user.getRole().equalsIgnoreCase("SCHEDULER")) {
                new SchedulerDashboard((models.Scheduler) user);
            } else if (user.getRole().equalsIgnoreCase("MANAGER")) {
                new ManagerDashboard((models.Manager) user);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}