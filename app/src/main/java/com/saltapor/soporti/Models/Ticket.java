package com.saltapor.soporti.Models;

import java.io.Serializable;

public class Ticket implements Serializable {

    public String title;
    public Category category;
    public String description;
    public String state;
    public long date;
    public User user;
    public User admin;
    public String id;

    public Ticket() {}

    public Ticket(String title, Category category, String description, long date, User user, User admin, String state, String id) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.date = date;
        this.user = user;
        this.admin = admin;
        this.state = state;
        this.id = id;
    }

}
