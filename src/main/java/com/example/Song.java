package com.example;

public class Song {
    private String artist;
    private String title;
    private int duration;

    public Song(String artist, String title, int duration) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
