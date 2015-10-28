package com.example.galadar.stockxchange;

import java.util.ArrayList;

/**
 * Created by Galadar on 28/10/2015.
 */
public class Meeting {

    private int day;
    private String title;
    private ArrayList speech;

    public Meeting(int day, String title, ArrayList speech) {
        this.day = day;
        this.title = title;
        this.speech = speech;
    }

    public int getMeetingDay() {
        return day;
    }

    public String getMeetingTitle() {
        return title;
    }

    public ArrayList getMeetingSpeech() {
        return speech;
    }
}
