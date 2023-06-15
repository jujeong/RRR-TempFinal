package com.example.subwayjpgsqluse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentTime {
    private long mNow;
    private Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("hh:mm:ss");
    public String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}
