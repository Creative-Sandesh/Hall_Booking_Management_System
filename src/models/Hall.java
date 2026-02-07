package models;

public class Hall {
    private String id;
    private String name;
    private double pricePerHour;
    private int capacity;
    private boolean isMaintenance;

    public Hall(String id, String name, double pricePerHour, int capacity, boolean isMaintenance) {
        this.id = id;
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.capacity = capacity;
        this.isMaintenance = isMaintenance;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPricePerHour() { return pricePerHour; }
    public int getCapacity() { return capacity; }
    public boolean isMaintenance() { return isMaintenance; }

    // ==========================================
    //  THIS IS THE MISSING METHOD
    // ==========================================
    public String getType() {
        // Logic: Look for text inside parenthesis, e.g., "Hall A (Banquet)"
        if (name != null && name.contains("(") && name.endsWith(")")) {
            try {
                // Returns "Banquet"
                return name.substring(name.lastIndexOf("(") + 1, name.length() - 1);
            } catch (Exception e) {
                return "Standard";
            }
        }
        return "Standard"; // Default if no brackets found
    }

    // --- SETTERS ---
    public void setMaintenance(boolean maintenance) { isMaintenance = maintenance; }

    // --- TO STRING (For File Saving) ---
    public String toFileString() {
        return String.join(",", id, name, String.valueOf(pricePerHour), String.valueOf(capacity), String.valueOf(isMaintenance));
    }
}