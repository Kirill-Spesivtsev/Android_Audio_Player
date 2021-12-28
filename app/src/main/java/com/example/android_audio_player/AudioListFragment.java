package com.example.android_audio_player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.android_audio_player.databinding.FragmentAudioListBinding;

import java.io.File;
import java.util.ArrayList;


public class AudioListFragment extends Fragment {

    private FragmentAudioListBinding binding;

    private ListView listViewSongs;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentAudioListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();




        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AudioListFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        listViewSongs = requireView().findViewById(R.id.listview_songs);

        listViewSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                activity.currentSong = (Song) MainActivity.audioAdapter.getItem((int)l);
                NavHostFragment.findNavController(AudioListFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        requestPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        //((MainActivity)requireActivity()).getSupportActionBar().setTitle("Музыка");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MainActivity.ALL_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    displayContent();
                } else {
                    requestPermissions();
                }
                return;
        }
    }

    public void requestPermissions(){
        if (ContextCompat.checkSelfPermission(
                requireActivity().getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                        getActivity().getApplicationContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED)
        {
            displayContent();
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    MainActivity.ALL_PERMISSIONS_REQUEST_CODE);
        }
    }

    public ArrayList<Song> searchAudio(){

        ArrayList<Song> list = new ArrayList<>();

        ContentResolver cr = requireActivity().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {

                while(cur.moveToNext())
                {

                    @SuppressLint("Range") String fullPath =
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    @SuppressLint("Range") String fileName =
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    @SuppressLint("Range") String title =
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    @SuppressLint("Range") String artist =
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    Song song = new Song(fullPath, fileName, title, artist);
                    //Log.e("", data);
                    list.add(song);
                }
            }
            cur.close();
        }

        return list;
    }

    private void displayContent(){
        //Toast.makeText(requireActivity(), "You passed!", Toast.LENGTH_SHORT).show();

        //ExecutorService threadpool = Executors.newCachedThreadPool();
        //Future<ArrayList<Song>> futureTask = threadpool.submit(() -> searchAudio());;

        ArrayList<Song> songs = searchAudio();


        //try {
            //songs = futureTask.get();


        //threadpool.shutdown();

        //ArrayList<Song> songs = searchAudio();

        //if (songs.size() == 0){throw  new NullPointerException();}

        MainActivity.audioAdapter = new AudioAdapter(requireActivity(), songs);
        listViewSongs.setAdapter(MainActivity.audioAdapter);


    }

}