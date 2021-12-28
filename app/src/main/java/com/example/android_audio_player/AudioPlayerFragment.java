package com.example.android_audio_player;

import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android_audio_player.databinding.FragmentAudioPlayerBinding;

import java.io.IOException;

public class AudioPlayerFragment extends Fragment {

    private FragmentAudioPlayerBinding binding;

    Button btnPlay, btnNext, btnPrev, btnFF, btnRev;
    TextView strSongsTitle, strSongArtist, strSongCurTime, strSongMaxTime;
    SeekBar seekBarSongProgress;
    ImageView imageViewAlbum;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAudioPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InitViews();

        if (MainActivity.mediaPlayer != null){
            MainActivity.mediaPlayer.stop();
            MainActivity.mediaPlayer.release();
        }

        MainActivity.mediaPlayer = new MediaPlayer();
        try {
            MainActivity.mediaPlayer.setDataSource(MainActivity.currentSong.fullPath);
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.start();
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            strSongsTitle.setText(MainActivity.currentSong.title);
            strSongArtist.setText(MainActivity.currentSong.artist);

        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBarSongProgress.setMax(MainActivity.mediaPlayer.getDuration());

        seekBarSongProgress.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBarSongProgress.getThumb().setColorFilter(getResources()
                .getColor(R.color.white),PorterDuff.Mode.SRC_IN);

        String endTime = buildTimeStamp(MainActivity.mediaPlayer.getDuration());
        strSongMaxTime.setText(endTime);

        MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        seekBarSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String newTime = buildTimeStamp(progress);
                strSongCurTime.setText(newTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.mediaPlayer.seekTo(seekBar.getProgress());
                String newTime = buildTimeStamp(MainActivity.mediaPlayer.getCurrentPosition());
                strSongCurTime.setText(newTime);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    MainActivity.mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    MainActivity.mediaPlayer.start();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public String buildTimeStamp(int duration){
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        String time = min + ":" + String.format("%02d", sec);
        return time;
    }

    public void InitViews(){
        btnPrev = requireView().findViewById(R.id.button_prev);
        btnNext = requireView().findViewById(R.id.button_next);
        btnPlay = requireView().findViewById(R.id.button_play);
        btnFF = requireView().findViewById(R.id.button_ff);
        btnRev = requireView().findViewById(R.id.button_rev);
        strSongsTitle = requireView().findViewById(R.id.text_song_title);
        strSongArtist = requireView().findViewById(R.id.text_song_artist);
        strSongCurTime = requireView().findViewById(R.id.txtsstart);
        strSongMaxTime = requireView().findViewById(R.id.txtsstop);
        seekBarSongProgress = requireView().findViewById(R.id.seekbar);
        imageViewAlbum = requireView().findViewById(R.id.imageview);
    }

}