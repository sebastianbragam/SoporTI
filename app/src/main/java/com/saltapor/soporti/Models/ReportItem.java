package com.saltapor.soporti.Models;

public class ReportItem {

    public String title;
    public Long quantity;
    public long time;

    public ReportItem () { }

    public ReportItem (String title, Long quantity, long time) {
        this.title = title;
        this.quantity = quantity;
        this.time = time;
    }

}
