package models;

import java.io.Serializable;

public abstract class User implements Serializable {

    protected String id;
    protected String username;
    protected String password;
    protected String name;
    protected String email;


    // constructor

    public User(String id, String username, String password,String name, String email){
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;


    }

    // getter methods
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    //Abstract methods
    public abstract String getRole();
    public abstract String toFileString();

}
