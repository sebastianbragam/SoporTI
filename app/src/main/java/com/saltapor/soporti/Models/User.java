package com.saltapor.soporti.Models;

import java.io.Serializable;

public class User implements Serializable {

    public String firstName;
    public String lastName;
    public String email;
    public String id;
    public String type;

    public User() {}

    public User(String firstName, String lastName, String email, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.id = id;
    }

}
