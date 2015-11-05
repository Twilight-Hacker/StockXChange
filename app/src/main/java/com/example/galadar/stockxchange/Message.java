package com.example.galadar.stockxchange;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Galadar on 18/10/2015.
 */
public class Message{
    String title;
    String body;

    public Message(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Message(int type, int magnitude){


    }


    public String getTitle() {
        return title;
    }


    public String getBody() {
        return body;
    }


}
