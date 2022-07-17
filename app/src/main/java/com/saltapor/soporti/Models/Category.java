package com.saltapor.soporti.Models;

import java.io.Serializable;

public class Category implements Serializable {

    public String category;
    public String subcategory;
    public String id;

    public Category() {}

    public Category(String category, String subcategory, String id) {
        this.category = category;
        this.subcategory = subcategory;
        this.id = id;
    }

}
