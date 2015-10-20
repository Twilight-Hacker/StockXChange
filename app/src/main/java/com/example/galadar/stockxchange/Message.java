package com.example.galadar.stockxchange;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Galadar on 18/10/2015.
 */
public class Message implements Parcelable {
    int duration;
    String title;
    String body;

    public Message(int duration, String title, String body) {
        this.duration = duration;
        this.title = title;
        this.body = body;
    }

    protected Message(Parcel in) {
        duration = in.readInt();
        title = in.readString();
        body = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(duration);
        dest.writeString(title);
        dest.writeString(body);
    }
}
