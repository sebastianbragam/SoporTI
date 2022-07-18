package com.saltapor.soporti.Models;

import java.io.Serializable;

public class Reply implements Serializable {

    public String id;
    public String reply;
    public long date;
    public User user;

    public Reply() {}

    public Reply(String id, String reply, long date, User user) {
        this.id = id;
        this.reply = reply;
        this.date = date;
        this.user = user;
    }
}
