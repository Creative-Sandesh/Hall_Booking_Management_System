package views;

import models.Hall;
import services.FileHandler;
import utils.IdGenerator;

import javax.swing.*;
import java.awt.*;

public class HallForm extends JFrame {
    public HallForm() {
        setTitle("Add New Hall");
        setSize(350, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JTextField txtName = new JTextField();
        String[] types = {"Auditorium", "Banquet", "Meeting Room"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        JTextField txtCap = new JTextField();
        JTextField txtPrice = new JTextField();

        panel.add(new JLabel("Hall Name:")); panel.add(txtName);
        panel.add(new JLabel("Hall Type:")); panel.add(cmbType);
        panel.add(new JLabel("Capacity:")); panel.add(txtCap);
        panel.add(new JLabel("Price (RM/hr):")); panel.add(txtPrice);

        JButton btnSave = new JButton("Save Hall");
        btnSave.addActionListener(e -> {
            try {
                String id = IdGenerator.generateNextId("HALL");
                String name = txtName.getText();
                String type = (String) cmbType.getSelectedItem(); // Note: Your Hall model stores type inside name or separately? Check model.
                // Assuming your Hall model constructor is (id, name, price, capacity, maintenance)
                // If you want to store "Type", you might append it to name like "Banquet Hall A"

                double price = Double.parseDouble(txtPrice.getText());
                int cap = Integer.parseInt(txtCap.getText());

                Hall h = new Hall(id, name + " (" + type + ")", price, cap, false);

                if(FileHandler.addHall(h)) {
                    JOptionPane.showMessageDialog(this, "Hall Added!");
                    dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Number Format");
            }
        });

        add(panel, BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
    }
}