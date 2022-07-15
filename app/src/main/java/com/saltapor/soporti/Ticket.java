package com.saltapor.soporti;

import java.util.Date;

public class Ticket {

    public String title;
    public String category;
    public String description;
    public long date;
    public User user;

    public Ticket() {}

    public Ticket(String title, String category, String description, long date, User user) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.date = date;
        this.user = user;
    }

}
