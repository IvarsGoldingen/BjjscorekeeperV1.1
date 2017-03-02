package com.example.android.bjjscorekeeper;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import static com.example.android.bjjscorekeeper.R.id.minutes;
import static com.example.android.bjjscorekeeper.R.id.red_advantage;

public class MainActivity extends AppCompatActivity {
    static int CTDWN_INTERVAL = 100;
    int redPoints=0;
    int redAdvantages=0;
    int bluePoints=0;
    int blueAdvantages=0;
    int nextScoreNumber =1;
    ArrayList<Score> scores = new ArrayList<Score>();
    CountDownTimer cdTimer = null;
    Boolean timerPaused = false;
    long milisecondsLeft = 0;
    TextView minutesField;
    TextView secondsField;
    Button startPauseResumeButton;

    int lastSetMinutes=5;
    int lastSetSeconds=0;

    NumberFormat twoDigitFormat = new DecimalFormat("00");

    MediaPlayer myMediaPlayer;
    private AudioManager myAudioManager;
    private AudioManager.OnAudioFocusChangeListener myOnAudioFocusListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange==AudioManager.AUDIOFOCUS_LOSS){
                        releaseMediaPlayer();
                    }
                    else if (focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT||
                            focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
                    {
                        myMediaPlayer.pause();
                        myMediaPlayer.seekTo(0);//start the sound from beginning on return
                    }
                    else if (focusChange==AudioManager.AUDIOFOCUS_GAIN){
                        myMediaPlayer.start();
                    }
                }
            };
    MediaPlayer.OnCompletionListener myOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {       //restore value

        //restore the scores
        redPoints = savedInstanceState.getInt("redPointsBackup");
        redAdvantages = savedInstanceState.getInt("redAdvantagesBackup");
        bluePoints = savedInstanceState.getInt("bluePointsBackup");
        blueAdvantages = savedInstanceState.getInt("blueAdvantagesBackup");
        super.onRestoreInstanceState(savedInstanceState);
        refreshAll();

        //restore time
        lastSetMinutes = savedInstanceState.getInt("lastSetMinutes");
        lastSetSeconds = savedInstanceState.getInt("lastSetSeconds");
        milisecondsLeft=savedInstanceState.getLong("milisecondsLeft");
        if(savedInstanceState.getBoolean("timerWasRunnin")){
            //if timer was running, resume
            startTimer(milisecondsLeft);
            startPauseResumeButton.setText(R.string.pause);
        }
        else{
            //else timer was not running at all or paused
            minutesField.setText(twoDigitFormat.format(savedInstanceState.getInt("timerMinutesShowing")));
            secondsField.setText(twoDigitFormat.format(savedInstanceState.getInt("timerSecondsShowing")));
            if(savedInstanceState.getBoolean("timerWasPaused")){
                //if timer was paused set the correct button name
                startPauseResumeButton.setText(R.string.resume);
                timerPaused=true;
            }

        }

        //restore array
        scores = savedInstanceState.getParcelableArrayList("history");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {                    // save varible to temporary varible
        //save scores
        super.onSaveInstanceState(outState);
        outState.putInt("redPointsBackup",redPoints);
        outState.putInt("redAdvantagesBackup",redAdvantages);
        outState.putInt("bluePointsBackup",bluePoints);
        outState.putInt("blueAdvantagesBackup",blueAdvantages);

        //save timer
        if (cdTimer != null) {
            cdTimer.cancel();//deelte the timer or else there will be 2 timers running when the app continues
        }
        outState.putInt("lastSetMinutes",lastSetMinutes);
        outState.putInt("lastSetSeconds",lastSetSeconds);
        outState.putBoolean("timerWasPaused",timerPaused);
        if (cdTimer==null) {//no timer was running
            outState.putBoolean("timerWasRunnin",false);
        }
        else{
            outState.putBoolean("timerWasRunnin",true);
        }
        outState.putLong("milisecondsLeft",milisecondsLeft);
        outState.putInt("timerMinutesShowing",Integer.valueOf((minutesField.getText().toString())));
        outState.putInt("timerSecondsShowing",Integer.valueOf((secondsField.getText().toString())));

        //save history
        outState.putParcelableArrayList("history", scores);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        minutesField = (TextView) findViewById(minutes);
        secondsField = (TextView)findViewById(R.id.seconds);

        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeBackLastMove();
            }
        });

        ImageButton plusMinutes = (ImageButton)findViewById(R.id.minutes_plus);
        plusMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cdTimer==null&&!timerPaused){//allow time changes only if no timer is running and not paused
                    long minutes = Integer.valueOf((minutesField.getText().toString()));
                    if (minutes>=99){
                        minutes=-1;
                    }
                    minutesField.setText(twoDigitFormat.format(minutes+1));
                }
            }
        });

        ImageButton minusMinutes = (ImageButton)findViewById(R.id.minutes_minus);
        minusMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cdTimer==null&&!timerPaused){//allow time changes only if no timer is running and not paused
                    long minutes = Integer.valueOf((minutesField.getText().toString()));
                    if (minutes<=0){
                        minutes=100;
                    }
                    minutesField.setText(twoDigitFormat.format(minutes-1));
                }
            }
        });

        ImageButton plusSeconds = (ImageButton)findViewById(R.id.seconds_plus);
        plusSeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cdTimer==null&&!timerPaused){//allow time changes only if no timer is running and not paused
                    long seconds = Integer.valueOf((secondsField.getText().toString()));
                    if (seconds>=59){
                        seconds=-1;
                    }
                    secondsField.setText(twoDigitFormat.format(seconds+1));
                }
            }
        });

        ImageButton minusSeconds = (ImageButton)findViewById(R.id.seconds_minus);
        minusSeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cdTimer==null&&!timerPaused){//allow time changes only if no timer is running and not paused
                    long seconds = Integer.valueOf((secondsField.getText().toString()));
                    if (seconds<=0){
                        seconds=60;
                    }
                    secondsField.setText(twoDigitFormat.format(seconds-1));
                }
            }
        });

        Button resetTimeButton = (Button) findViewById(R.id.reset_timer);
        resetTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        startPauseResumeButton = (Button) findViewById(R.id.start_pause_resume_timer);
        startPauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test code to use ony 1 button
                //if there is no timer running and no timer paused we need to start a new timer
                if (cdTimer == null && !timerPaused) {
                    long minutes = Integer.valueOf((minutesField.getText().toString()));
                    long seconds = Integer.valueOf((secondsField.getText().toString()));
                    //check if set time is more than a second
                    long timeInMillis = (minutes * 60 * 1000) + (seconds * 1000);
                    if (timeInMillis > 0) {
                        //save the "last set" variables only on a new timer launch
                        lastSetMinutes = (int) minutes;
                        lastSetSeconds = (int) seconds;
                        startTimer(timeInMillis);
                        startPauseResumeButton.setText(R.string.pause);
                    } else {
                        //show a toast message that we need a valid time value
                        Toast myToast = Toast.makeText(MainActivity.this, R.string.no_time_error, Toast.LENGTH_SHORT);
                        myToast.show();
                    }

                }
                //if there is a paused timer it need to be resumed
                else if (timerPaused) {
                    resumeTimer();
                }
                //the last option is that is left is that the timer has to be paused
                else {
                    pauseTimer();
                }

            }
        });


        Button bluePoints4 = (Button)findViewById(R.id.blue_4points);
        bluePoints4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points4)));
                bluePoints+=4;
                refreshPointsBlue();
            }
        });

        Button bluePoints3 = (Button)findViewById(R.id.blue_3points);
        bluePoints3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points3)));
                bluePoints+=3;
                refreshPointsBlue();
            }
        });

        Button bluePoints2 = (Button)findViewById(R.id.blue_2points);
        bluePoints2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points2)));
                bluePoints+=2;
                refreshPointsBlue();
            }
        });

        Button blueAdvantage = (Button)findViewById(R.id.blue_advantage);
        blueAdvantage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.advantage)));
                blueAdvantages+=1;
                refreshAdvantagesBlue();
            }
        });

        Button redPoints4 = (Button)findViewById(R.id.red_4points);
        redPoints4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points4)));
                redPoints+=4;
                refreshPointsRed();
            }
        });

        Button redPoints3 = (Button)findViewById(R.id.red_3points);
        redPoints3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points3)));
                redPoints+=3;
                refreshPointsRed();
            }
        });

        Button redPoints2 = (Button)findViewById(R.id.red_2points);
        redPoints2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points2)));
                redPoints+=2;
                refreshPointsRed();
            }
        });

        Button redAdvantage = (Button)findViewById(R.id.red_advantage);
        redAdvantage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(nextScoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.advantage)));
                redAdvantages+=1;
                refreshAdvantagesRed();
            }
        });

        Button resetScoreButton = (Button)findViewById(R.id.reset_button);
        resetScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluePoints=0;
                redPoints=0;
                blueAdvantages=0;
                redAdvantages=0;
                nextScoreNumber =1;
                scores.clear();
                refreshAll();
            }
        });

        final Button historyButton = (Button)findViewById(R.id.history_button);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(MainActivity.this, historyActivity.class);
                historyIntent.putExtra("myList",scores);
                startActivity(historyIntent);
            }
        });

    }
    private void refreshAll(){
        refreshPointsRed();
        refreshPointsBlue();
        refreshAdvantagesBlue();
        refreshAdvantagesRed();
    }
    private void refreshPointsBlue(){
        TextView bluePointsView = (TextView)findViewById(R.id.blue_points_view);
        bluePointsView.setText(String.valueOf(bluePoints));
    }
    private void refreshPointsRed(){
        TextView redPointsView = (TextView)findViewById(R.id.red_points_view);
        redPointsView.setText(String.valueOf(redPoints));
    }
    private void refreshAdvantagesBlue(){
        TextView blueAdvantagesView = (TextView)findViewById(R.id.blue_advantages_view);
        blueAdvantagesView.setText(String.valueOf(blueAdvantages));
    }
    private void refreshAdvantagesRed(){
        TextView redAdvantagesView = (TextView)findViewById(R.id.red_advantages_view);
        redAdvantagesView.setText(String.valueOf(redAdvantages));
    }

    private void resetTimer(){
        if(cdTimer!=null){
            cdTimer.cancel();
            cdTimer = null;
        }
        minutesField.setText(twoDigitFormat.format(lastSetMinutes));
        secondsField.setText(twoDigitFormat.format(lastSetSeconds));
        timerPaused=false;
        startPauseResumeButton.setText(R.string.start);
    }

    private void startTimer(long timerTime){
        if (cdTimer == null) {
            cdTimer = new CountDownTimer(timerTime, CTDWN_INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                    minutesField.setText(twoDigitFormat.format(millisUntilFinished / 1000/60));
                    secondsField.setText(twoDigitFormat.format((millisUntilFinished / 1000)%60));
                    milisecondsLeft = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    minutesField.setText("00");
                    secondsField.setText("00");
                    startPauseResumeButton.setText(R.string.start);
                    cdTimer=null;

                    //Sound effect
                    releaseMediaPlayer();
                    int result = myAudioManager.requestAudioFocus(myOnAudioFocusListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                        myMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.end);
                        myMediaPlayer.start();
                        myMediaPlayer.setOnCompletionListener(myOnCompletionListener);
                    }
                }
            }.start();
        }
    }

    private void pauseTimer() {
        if (cdTimer != null) {//check if there hass been soething before`
            startPauseResumeButton.setText(R.string.resume);
            timerPaused = true;
            cdTimer.cancel();
            cdTimer = null;
        }
    }

    private void resumeTimer() {
        startPauseResumeButton.setText(R.string.pause);
        timerPaused = false;
        startTimer(milisecondsLeft);
    }

    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (myMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            myMediaPlayer.release();
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            myMediaPlayer = null;
            myAudioManager.abandonAudioFocus(myOnAudioFocusListener);
        }
    }

    private void takeBackLastMove(){
        if (nextScoreNumber > 1) {
            //Check if there have been moves
            Score lastScore = scores.get(nextScoreNumber -2);//-2 because array start with 0
            if (lastScore.GetCornerScored()==getResources().getString(R.string.blue_corner)) {//wich corner scored
                if (lastScore.GetPointsScored()==getResources().getString(R.string.points4)) {
                    bluePoints-=4;
                }
                else if (lastScore.GetPointsScored()==getResources().getString(R.string.points3)){
                    bluePoints-=3;
                }
                else if (lastScore.GetPointsScored()==getResources().getString(R.string.points2)){
                    bluePoints-=2;
                }
                else{//else it is an advantage
                    blueAdvantages-=1;
                    refreshAdvantagesBlue();
                }
                scores.remove(nextScoreNumber -2);
                refreshPointsBlue();
            }
            else{//if not bue then red
                if (lastScore.GetPointsScored()==getResources().getString(R.string.points4)) {
                    redPoints-=4;
                }
                else if (lastScore.GetPointsScored()==getResources().getString(R.string.points3)){
                    redPoints-=3;
                }
                else if (lastScore.GetPointsScored()==getResources().getString(R.string.points2)){
                    redPoints-=2;
                }
                else{//else it is an advantage
                    redAdvantages-=1;
                    refreshAdvantagesRed();
                }
                scores.remove(nextScoreNumber -2);
                refreshPointsRed();
            }
            nextScoreNumber--;
        } else {
            Toast noMovesToReturnToast = Toast.makeText(this, getResources().getString(R.string.no_moves), Toast.LENGTH_SHORT);
            noMovesToReturnToast.show();
        }
    }
}
