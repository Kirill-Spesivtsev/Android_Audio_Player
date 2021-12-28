package com.example.android_audio_player;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class AudioAdapter extends BaseAdapter {

    ArrayList<Song> objects;

    Context context;

    LayoutInflater inflater;

    AudioAdapter(Context con, ArrayList<Song> obj){
        objects = obj;
        context = con;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return objects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.list_item,null);
        TextView songTitle = v.findViewById(R.id.textview_song_title);
        TextView songArtist = v.findViewById(R.id.textview_song_artist);
        //songTitle.setSelected(true);
        //songTitle.setTypeface(Typeface.DEFAULT);
        songTitle.setText(objects.get(i).title);
        songArtist.setText(objects.get(i).artist);

        return v;
    }

    public int getPosition(Song s){
        return objects.indexOf(s);
    }

    public ArrayList<Song> getList(){
        return objects;
    }
}
