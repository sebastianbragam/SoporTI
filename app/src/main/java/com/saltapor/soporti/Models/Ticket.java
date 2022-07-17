package com.saltapor.soporti.Models;

public class Ticket {

    public String title;
    public Category category;
    public String description;
    public long date;
    public User user;

    public Ticket() {}

    public Ticket(String title, Category category, String description, long date, User user) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.date = date;
        this.user = user;
    }

}
