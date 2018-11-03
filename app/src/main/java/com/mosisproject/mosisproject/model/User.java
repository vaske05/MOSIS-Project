package com.mosisproject.mosisproject.model;

public class User {
    public String name;
    public String surname;
    public String phone;
    public String email;

    public User() {
    }

    public User(String name, String surname, String email, String phone) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
    }
}
