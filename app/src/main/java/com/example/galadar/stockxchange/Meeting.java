package com.example.galadar.stockxchange;

import java.util.ArrayList;

/**
 * Created by Galadar on 28/10/2015.
 * Meeting Object
 */
public class Meeting {

    private int day;
    private String title;
    private ArrayList<String> speech;

    public Meeting(int day, String title, ArrayList<String> speech) {
        this.day=day;
        this.title = title;
        this.speech = speech;
    }

    public int getDay() {
        return this.day;
    }

    public String getMeetingTitle() {
        return this.title;
    }

    public ArrayList<String> getMeetingSpeech() {
        return this.speech;
    }
}
