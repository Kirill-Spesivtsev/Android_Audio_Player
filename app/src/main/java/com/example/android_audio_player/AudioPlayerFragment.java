package com.example.android_audio_player;

import static android.os.SystemClock.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.android_audio_player.databinding.FragmentAudioPlayerBinding;

import java.io.IOException;

public class AudioPlayerFragment extends Fragment {

    private FragmentAudioPlayerBinding binding;

    private boolean isPlayerReleased = true;

    Button btnPlay, btnNext, btnPrev, btnFF, btnRew;
    TextView strSongsTitle, strSongArtist, strSongCurTime, strSongMaxTime;
    SeekBar seekBarSongProgress;
    ImageView imageViewAlbum;
    LinearLayout playerParentLayout;

    Thread updateSeekBarPositionThread;


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

        initViews();

        initStartSong();

        prepareResponsiveSeekbar();

        MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        seekBarSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String newTime = buildTimeStamp(MainActivity.mediaPlayer.getCurrentPosition());
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

        playerParentLayout.setOnTouchListener(new OnSwipeTouchListener(requireActivity()){
            public void onSwipeTop() {

            }
            public void onSwipeRight() {
                btnNext.performClick();
            }
            public void onSwipeLeft() {
                btnPrev.performClick();
            }
            public void onSwipeBottom() {
                NavHostFragment.findNavController(AudioPlayerFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToNextSong();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToPrevSong();
            }
        });

        btnFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastForwardSong();
            }
        });

        btnRew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewindSong();
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
        updateSeekBarPositionThread.interrupt();
    }

    public void initViews(){
        btnPrev = requireView().findViewById(R.id.button_prev);
        btnNext = requireView().findViewById(R.id.button_next);
        btnPlay = requireView().findViewById(R.id.button_play);
        btnFF = requireView().findViewById(R.id.button_ff);
        btnRew = requireView().findViewById(R.id.button_rew);
        strSongsTitle = requireView().findViewById(R.id.text_song_title);
        strSongArtist = requireView().findViewById(R.id.text_song_artist);
        strSongCurTime = requireView().findViewById(R.id.text_current_time);
        strSongMaxTime = requireView().findViewById(R.id.text_max_time);
        seekBarSongProgress = requireView().findViewById(R.id.seekbar);
        imageViewAlbum = requireView().findViewById(R.id.image_album);
        playerParentLayout = requireView().findViewById(R.id.player_parent_layout);
    }

    private void prepareResponsiveSeekbar(){
        updateSeekBarPositionThread = new Thread(){
            @Override
            public void run() {
                updateSeekBar();
            }
        };

        seekBarSongProgress.setMax(MainActivity.mediaPlayer.getDuration());
        updateSeekBarPositionThread.start();

        seekBarSongProgress.getProgressDrawable().setColorFilter(getResources()
                .getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBarSongProgress.getThumb().setColorFilter(getResources()
                .getColor(R.color.white),PorterDuff.Mode.SRC_IN);

        String endTime = buildTimeStamp(MainActivity.mediaPlayer.getDuration());
        strSongMaxTime.setText(endTime);
    }

    private void initStartSong(){
        if (MainActivity.mediaPlayer != null){
            isPlayerReleased = true;
            seekBarSongProgress.setProgress(0);
            MainActivity.mediaPlayer.stop();
            MainActivity.mediaPlayer.release();
        }

        MainActivity.mediaPlayer = new MediaPlayer();
        try {
            MainActivity.mediaPlayer.setDataSource(MainActivity.currentSong.fullPath);
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.start();
            isPlayerReleased = false;
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            strSongsTitle.setText(MainActivity.currentSong.title);
            strSongsTitle.setSelected(true);
            strSongArtist.setText(MainActivity.currentSong.artist);

            createNotificationChannel();
            setNotification(MainActivity.currentSong.title, MainActivity.currentSong.artist);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playPauseSong(){
        if (MainActivity.mediaPlayer.isPlaying()){
            btnPlay.setBackgroundResource(R.drawable.ic_play);
            MainActivity.mediaPlayer.pause();
        }
        else{
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            MainActivity.mediaPlayer.start();
        }
    }

    private void skipToNextSong(){
        btnNext.setEnabled(false);
        btnPrev.setEnabled(false);
        isPlayerReleased = true;
        seekBarSongProgress.setProgress(0);
        MainActivity.mediaPlayer.stop();
        MainActivity.mediaPlayer.release();
        int position = MainActivity.audioAdapter.getPosition(MainActivity.currentSong);
        position = ((position + 1) % MainActivity.audioAdapter.getCount());
        MainActivity.currentSong = (Song) MainActivity.audioAdapter.getItem(position);

        MainActivity.mediaPlayer = new MediaPlayer();
        try {
            MainActivity.mediaPlayer.setDataSource(MainActivity.currentSong.fullPath);
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.start();
            isPlayerReleased = false;
            seekBarSongProgress.setMax(MainActivity.mediaPlayer.getDuration());
            String endTime = buildTimeStamp(MainActivity.mediaPlayer.getDuration());
            strSongMaxTime.setText(endTime);
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            strSongsTitle.setText(MainActivity.currentSong.title);
            strSongsTitle.setSelected(true);
            strSongArtist.setText(MainActivity.currentSong.artist);
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnNext.setEnabled(true);
        btnPrev.setEnabled(true);
    }

    private void skipToPrevSong(){
        btnNext.setEnabled(false);
        btnPrev.setEnabled(false);
        isPlayerReleased = true;
        seekBarSongProgress.setProgress(0);
        MainActivity.mediaPlayer.stop();
        MainActivity.mediaPlayer.release();
        int position = MainActivity.audioAdapter.getPosition(MainActivity.currentSong);
        if (position <= 0){
            position = MainActivity.audioAdapter.getCount() - 1;
        }
        else{
            position--;
        }
        MainActivity.currentSong = (Song) MainActivity.audioAdapter.getItem(position);
        MainActivity.mediaPlayer = new MediaPlayer();
        try {
            MainActivity.mediaPlayer.setDataSource(MainActivity.currentSong.fullPath);
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.start();
            isPlayerReleased = false;
            seekBarSongProgress.setMax(MainActivity.mediaPlayer.getDuration());
            String endTime = buildTimeStamp(MainActivity.mediaPlayer.getDuration());
            strSongMaxTime.setText(endTime);
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            strSongsTitle.setText(MainActivity.currentSong.title);
            strSongsTitle.setSelected(true);
            strSongArtist.setText(MainActivity.currentSong.artist);
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnNext.setEnabled(true);
        btnPrev.setEnabled(true);
    }

    private void fastForwardSong(){
        if (MainActivity.mediaPlayer.isPlaying()){
            MainActivity.mediaPlayer.seekTo(
                    MainActivity.mediaPlayer.getCurrentPosition() + 5000);
        }
    }

    private void rewindSong(){
        if (MainActivity.mediaPlayer.isPlaying()){
            MainActivity.mediaPlayer.seekTo(
                    MainActivity.mediaPlayer.getCurrentPosition() - 5000);
        }
    }

    private void updateSeekBar(){
        int maxPos = MainActivity.mediaPlayer.getDuration();
        int curPos = MainActivity.mediaPlayer.getCurrentPosition();
        while(curPos < maxPos){
            if (MainActivity.mediaPlayer != null && !isPlayerReleased
                    && MainActivity.mediaPlayer.isPlaying())
            {
                maxPos = MainActivity.mediaPlayer.getDuration();
                curPos = MainActivity.mediaPlayer.getCurrentPosition();
                seekBarSongProgress.setProgress(curPos);
            }
            sleep(1000);
        }
    };

    public String buildTimeStamp(int duration){
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return min + ":" + String.format("%02d", sec);
    }

    private void buildMediaSession(){
        MainActivity.mediaSession = new MediaSession(requireActivity(),"main audio player session");
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY
                                | PlaybackState.ACTION_STOP
                                | PlaybackState.ACTION_PAUSE
                                | PlaybackState.ACTION_PLAY_PAUSE
                                | PlaybackState.ACTION_SKIP_TO_NEXT
                                | PlaybackState.ACTION_SKIP_TO_PREVIOUS);
        MainActivity.mediaSession.setMediaButtonReceiver(null);
        MainActivity.mediaSession.setPlaybackState(stateBuilder.build());
        MainActivity.mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                //playPauseSong();
                setNotification(MainActivity.currentSong.title, MainActivity.currentSong.artist);

            }

            @Override
            public void onPause() {
                super.onPause();
                //playPauseSong();
                setNotification(MainActivity.currentSong.title, MainActivity.currentSong.artist);
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
            }
        });
    }


    private  void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channelName";
            String description = "channelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("customNotificationChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void setNotification(String notificationTitle, String notificationText){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(requireActivity(), "customNotificationChannel")
                    .setSmallIcon(R.drawable.ic_m_3)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_m_3))
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setPriority(Notification.PRIORITY_HIGH);
                    //.setStyle(new Notification.MediaStyle();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(requireActivity());
        notificationManager.notify(101, builder.build());
        }
    }
}