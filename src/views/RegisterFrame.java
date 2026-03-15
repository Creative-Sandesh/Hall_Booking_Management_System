package views;

import models.Customer;
import services.AuthService; // We will add the logic here next
import services.FileHandler;
import utils.IdGenerator; // Use the utility we made earlier

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField nameField, emailField, phoneField, userField;
    private JPasswordField passField;

    // PALETTE
    private final Color PRIMARY_COLOR = new Color(30, 58, 138);
    private final Color BG_COLOR = new Color(243, 244, 246);

    public RegisterFrame() {
        setTitle("Customer Registration");
        setSize(400, 550); // Taller window for more fields
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(7, 1, 10, 10)); // 7 Rows
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Title
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(PRIMARY_COLOR);
        mainPanel.add(title);

        // Inputs
        nameField = addField(mainPanel, "Full Name:");
        emailField = addField(mainPanel, "Email Address:");
        phoneField = addField(mainPanel, "Phone Number:");
        userField = addField(mainPanel, "Username:");

        // Password
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(BG_COLOR);
        passPanel.add(new JLabel("Password:"), BorderLayout.NORTH);
        passField = new JPasswordField();
        passPanel.add(passField, BorderLayout.CENTER);
        mainPanel.add(passPanel);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(BG_COLOR);

        JButton btnRegister = new JButton("Sign Up");
        btnRegister.setBackground(PRIMARY_COLOR);
        btnRegister.setForeground(Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(Color.GRAY);
        btnCancel.setForeground(Color.WHITE);

        btnPanel.add(btnRegister);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel);

        add(mainPanel);



        btnCancel.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        btnRegister.addActionListener(e -> registerCustomer());

        setVisible(true);
    }

    private JTextField addField(JPanel panel, String labelText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.add(new JLabel(labelText), BorderLayout.NORTH);
        JTextField tf = new JTextField();
        p.add(tf, BorderLayout.CENTER);
        panel.add(p);
        return tf;
    }

    private void registerCustomer() {
        // 1. Get Data
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        // 2. Validate (Basic)
        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        // validating email
        if(!email.contains("@") || !email.contains(".") || email.indexOf("@")>email.lastIndexOf(".")){
            JOptionPane.showMessageDialog(this, "Please enter valid email address! (e.g. user@example.com)","Invalid Email",JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Create Customer Object
        // ID is generated automatically using  Util
        String newId = IdGenerator.generateNextId("CUSTOMER");
        Customer newCustomer = new Customer(newId, user, pass, name, email, phone);

        // 4. Save to File
        boolean success = FileHandler.addUser(newCustomer);

        if (success) {
            JOptionPane.showMessageDialog(this, "Account Created Successfully!\nPlease Login.");
            dispose();
            new LoginFrame();
        } else {
            JOptionPane.showMessageDialog(this, "Error creating account.");
        }
    }
}