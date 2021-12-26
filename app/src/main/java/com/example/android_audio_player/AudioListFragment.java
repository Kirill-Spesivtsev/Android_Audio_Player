package com.example.android_audio_player;

import android.Manifest;
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
import android.widget.Toast;

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

    public AudioAdapter audioAdapter;



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

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AudioListFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        audioAdapter = ((MainActivity)requireActivity()).audioAdapter;
        listViewSongs = requireView().findViewById(R.id.listview_songs);

        requestPermissions();
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
                requireActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
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

    public ArrayList<File> searchAudio(){

        ArrayList<File> l = new ArrayList<>();

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
                    int i = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
                    String data = cur.getString(i);
                    Log.e("", data);
                    l.add(new File(data));
                }
            }
            cur.close();
        }

        return l;
    }

    private void displayContent(){
        //Toast.makeText(requireActivity(), "You passed!", Toast.LENGTH_SHORT).show();

        final ArrayList<File> songs = searchAudio();

        //if (songs.size() == 0){throw  new NullPointerException();}

        ArrayList<String> items = new ArrayList<String>(songs.size());

        for (int i = 0; i < songs.size(); i++){
            items.add(i, songs.get(i).getName().replace(".mp3", ""));
        }

        audioAdapter = new AudioAdapter(requireActivity(), items);
        listViewSongs.setAdapter(audioAdapter);

        listViewSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String name = (String) listViewSongs.getItemAtPosition(position);

            }
        });

    }

}