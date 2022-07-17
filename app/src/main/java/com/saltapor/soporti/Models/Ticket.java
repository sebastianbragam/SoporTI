package com.saltapor.soporti.Models;

public class Ticket {

    public String title;
    public Category category;
    public String description;
    public String state;
    public long date;
    public User user;
    public String id;

    public Ticket() {}

    public Ticket(String title, Category category, String description, long date, User user, String state, String id) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.date = date;
        this.user = user;
        this.state = state;
        this.id = id;
    }

}
