package com.saltapor.soporti.Models;

public class ReportItem {

    public String title;
    public Long quantity;
    public long time;
    public Long responseQuantity;
    public long responseTime;
    public Long rating;

    public ReportItem () { }

    public ReportItem (String title, Long quantity, long time, Long responseQuantity, long responseTime, Long rating) {
        this.title = title;
        this.quantity = quantity;
        this.time = time;
        this.responseQuantity = responseQuantity;
        this.responseTime = responseTime;
        this.rating = rating;
    }

}
