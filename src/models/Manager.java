package models;

public class Manager extends User {

    // Constructor matches the new User format
    public Manager(String id, String username, String password, String name, String email) {
        super(id, username, password, name, email);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    @Override
    public String toFileString() {
        return getId() + "," +
                getUsername() + "," +
                getPassword() + "," +
                getName() + "," +
                getEmail() + "," +
                "MANAGER";
    }
}