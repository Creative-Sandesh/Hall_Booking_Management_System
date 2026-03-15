package views;

import models.*;
import java.awt.*;
import javax.swing.*;

public class BaseDashboard extends JFrame {
    // --- GLOBAL COLORS (From your Palette) ---
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);     // Blue
    public static final Color COLOR_ADMIN   = new Color(220, 38, 38);     // Red
    public static final Color COLOR_SCHEDULER = new Color(13, 148, 136);  // Teal
    public static final Color COLOR_MANAGER = new Color(245, 158, 11);    // Amber

    public static final Color COLOR_SIDEBAR = new Color(55, 65, 81);      // Dark Grey
    public static final Color COLOR_BG      = new Color(243, 244, 246);   // Light Grey

    // Component
    protected JPanel contentPanel;
    private JPanel sidebarPanel;

    public BaseDashboard(String pageTitle, User user, Color headerColor){
        // 1. Frame Set up
        setTitle(pageTitle);
        setSize(1000,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. Build Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT,20,15));
        header.setBackground(headerColor);
        header.setPreferredSize(new Dimension(getWidth(),60));

        JLabel titleLabel = new JLabel("Hall Booking Management System | " + user.getName() + " (" + user.getRole() + ")");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(titleLabel);
        add(header, BorderLayout.NORTH);

        // 3. Build Sidebar Container
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS)); // Vertical Stack
        sidebarPanel.setBackground(COLOR_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(220, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Padding

        add(sidebarPanel, BorderLayout.WEST);

        // 4. Build Main Content Area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(COLOR_BG);
        contentPanel.add(new JLabel("Select an option from the menu.", SwingConstants.CENTER));
        add(contentPanel, BorderLayout.CENTER);



    }

    protected void addSidebarButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40)); // Fixed size
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addActionListener(action);

        // HOVER EFFECT
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(75, 85, 99)); // Slightly lighter gray
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(COLOR_SIDEBAR); // Back to normal
            }
        });

        // Add specific spacing
        sidebarPanel.add(btn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between buttons
    }

    // HELPER FUNCTION: Add Logout Button specially
    protected void addLogoutButton() {
        sidebarPanel.add(Box.createVerticalGlue()); // Pushes logout to bottom

        JButton btn = new JButton("Logout");
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setForeground(new Color(255, 100, 100)); // Light Red text
        btn.setBackground(COLOR_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> {
            dispose(); // Close current dashboard
            new LoginFrame(); // Open Login
        });

        sidebarPanel.add(btn);
    }

    // HELPER FUNCTION: Swap the center screen
    protected void setPage(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}



