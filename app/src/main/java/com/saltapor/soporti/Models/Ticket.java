package com.saltapor.soporti.Models;

import java.io.Serializable;
import java.util.HashMap;

public class Ticket implements Serializable {

    public String title;
    public Category category;
    public String type;
    public String priority;
    public String description;
    public String state;
    public long date;
    public long finishDate;
    public User user;
    public User admin;
    public Long number;
    public Long rate;
    public HashMap<String, Reply> replies;
    public String id;

    public Ticket() {}

    public Ticket(String title, Category category, String type, String priority, String description, long date, User user, User admin, String state, Long number, String id) {
        this.title = title;
        this.category = category;
        this.type = type;
        this.priority = priority;
        this.description = description;
        this.date = date;
        this.user = user;
        this.admin = admin;
        this.state = state;
        this.number = number;
        this.id = id;
    }

}
