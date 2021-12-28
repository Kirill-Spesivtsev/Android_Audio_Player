package com.example.android_audio_player;

public class Song {
    public String  fullPath;

    public String fileName;

    public String title;

    public String artist;

    public Song(String fPath, String fName, String sTitle, String sArtist){
        fullPath = fPath;
        fileName = fName;
        title = sTitle;
        artist = sArtist;
    }
}
