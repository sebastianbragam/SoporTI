package com.saltapor.soporti.Models;

import android.net.Uri;

public class File {

    public String fileName;
    public String filePath;

    public File () { }

    public File (String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }
}
