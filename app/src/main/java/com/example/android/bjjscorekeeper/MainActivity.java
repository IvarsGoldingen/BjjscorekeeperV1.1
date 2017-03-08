package com.example.android.bjjscorekeeper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.android.bjjscorekeeper.R.id.minutes;
import static com.example.android.bjjscorekeeper.R.id.seconds;

public class MainActivity extends AppCompatActivity {

    //values for the preferences implementation
    private MyPreferensesClass myPreferences;
    private TextView minutesField;
    private TextView secondsField;
    private Button startPauseResumeButton;
    private int lastSetMinutes = 5;
    private int lastSetSeconds = 0;
    private NumberFormat twoDigitFormat = new DecimalFormat("00");
    private MediaPlayer myMediaPlayer;
    private TimerState myTimerState = TimerState.STOPPED;
    private int redPoints = 0;
    private int redAdvantages = 0;
    private int bluePoints = 0;
    private int blueAdvantages = 0;
    private int nextScoreNumber = 1;
    private ArrayList<Score> scores = new ArrayList<Score>();
    private CountDownTimer cdTimer = null;
    private long milisecondsLeft = 0;
    private long lastSetTimeMillis;
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
    private MediaPlayer.OnCompletionListener myOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {       //restore value

        super.onRestoreInstanceState(savedInstanceState);

        //restore the scores
        redPoints = savedInstanceState.getInt("redPointsBackup");
        redAdvantages = savedInstanceState.getInt("redAdvantagesBackup");
        bluePoints = savedInstanceState.getInt("bluePointsBackup");
        blueAdvantages = savedInstanceState.getInt("blueAdvantagesBackup");

        refreshUIPoints();

        //restore last set new time
        lastSetMinutes = savedInstanceState.getInt("lastSetMinutes");
        lastSetSeconds = savedInstanceState.getInt("lastSetSeconds");

        //restore history array
        scores = savedInstanceState.getParcelableArrayList("history");

        //resstore timer state
        myTimerState = (TimerState) savedInstanceState.getSerializable("timerState");
        updateStartPauseResumeButtonText();
        if (myTimerState == TimerState.PAUSED) {
            milisecondsLeft = savedInstanceState.getLong("pausedTimeLeft");
            Log.v("P1 restored millis", String.valueOf(milisecondsLeft));
        } else if (myTimerState == TimerState.STOPPED) {
            minutesField.setText(twoDigitFormat.format(savedInstanceState.getInt("minutesUI")));
            secondsField.setText(twoDigitFormat.format(savedInstanceState.getInt("secondsUI")));
        }

        //restore last set new or continued time
        lastSetTimeMillis = savedInstanceState.getLong("lastSetTimeMillis");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save variables to temporary varible
        //save scores
        super.onSaveInstanceState(outState);
        outState.putInt("redPointsBackup",redPoints);
        outState.putInt("redAdvantagesBackup",redAdvantages);
        outState.putInt("bluePointsBackup",bluePoints);
        outState.putInt("blueAdvantagesBackup",blueAdvantages);

        //save last set new time
        outState.putInt("lastSetMinutes", lastSetMinutes);
        outState.putInt("lastSetSeconds", lastSetSeconds);

        //save last set new or resumed time
        outState.putLong("lastSetTimeMillis", lastSetTimeMillis);

        //save history
        outState.putParcelableArrayList("history", scores);

        //save timer state
        outState.putSerializable("timerState", myTimerState);
        if (myTimerState == TimerState.PAUSED) {
            //if timer was paused, the time value has to be saved here
            outState.putLong("pausedTimeLeft", milisecondsLeft);
            Log.v("P1 saved millis", String.valueOf(milisecondsLeft));
        } else if (myTimerState == TimerState.STOPPED) {
            //if timer was stopped, save the current UI time settings
            outState.putInt("minutesUI", Integer.valueOf((minutesField.getText().toString())));
            outState.putInt("secondsUI", Integer.valueOf((secondsField.getText().toString())));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //of timer was running
        if (cdTimer != null) {
            cdTimer.cancel();//deelte the timer or else there will be 2 timers running when the app continues
            cdTimer = null;
            //set an alarm, so the user stil knows when the time ended
            setAlarm();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("P1 onResume()  ", String.valueOf(myTimerState));
        if (myTimerState == TimerState.RUNNING) {
            initTimerAfterAppPause();
        } else if (myTimerState == TimerState.PAUSED) {
            //if timer was paused we update the UI with the correct time
            updateTimeUI(milisecondsLeft);
        }

        //cancel the notification alarm because the app is open
        removeAlarm();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //creating a preferences object
        myPreferences = new MyPreferensesClass(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        minutesField = (TextView) findViewById(minutes);
        secondsField = (TextView) findViewById(seconds);

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
                if (myTimerState == TimerState.STOPPED) {//allow time changes only if no timer is stopped
                    int minutes = Integer.valueOf((minutesField.getText().toString()));
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
                if (myTimerState == TimerState.STOPPED) {//allow time changes only if no timer is stopped
                    int minutes = Integer.valueOf((minutesField.getText().toString()));
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
                if (myTimerState == TimerState.STOPPED) {//allow time changes only if no timer is stopped
                    int seconds = Integer.valueOf((secondsField.getText().toString()));
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
                if (myTimerState == TimerState.STOPPED) {//allow time changes only if no timer is stopped
                    int seconds = Integer.valueOf((secondsField.getText().toString()));
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
                updateStartPauseResumeButtonText();
            }
        });

        startPauseResumeButton = (Button) findViewById(R.id.start_pause_resume_timer);
        startPauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if there is no timer running and no timer paused we need to start a new timer
                if (myTimerState == TimerState.STOPPED) {
                    if (cdTimer == null) {
                        //The start time is saved in preferences
                        myPreferences.setStartedTime(getNow());

                        long minutes = Integer.valueOf((minutesField.getText().toString()));
                        long seconds = Integer.valueOf((secondsField.getText().toString()));
                        //check if set time is more than a second
                        long timeInMillis = (minutes * 60 * 1000) + (seconds * 1000);
                        if (timeInMillis > 0) {
                            //save the "last set" variables only on a new timer launch
                            lastSetMinutes = (int) minutes;
                            lastSetSeconds = (int) seconds;
                            lastSetTimeMillis = timeInMillis;
                            startTimer(timeInMillis);
                        } else {
                            //show a toast message that we need a valid time value
                            Toast myToast = Toast.makeText(MainActivity.this, R.string.no_time_error, Toast.LENGTH_SHORT);
                            myToast.show();
                        }
                    }
                }
                //if there is a paused timer it need to be resumed
                else if (myTimerState == TimerState.PAUSED) {
                    resumeTimer();
                }
                //the last option is that is left is that the timer has to be paused
                else {
                    pauseTimer();
                }
                updateStartPauseResumeButtonText();
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
                refreshUIPoints();
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

    private void refreshUIPoints() {
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
        myTimerState = TimerState.STOPPED;
    }

    private void startTimer(long timerTime){

        if (cdTimer == null) {
            int CTDWN_INTERVAL = 200;
            cdTimer = new CountDownTimer(timerTime, CTDWN_INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimeUI(millisUntilFinished);
                    //minutesField.setText(twoDigitFormat.format(millisUntilFinished / 1000/60));
                    //secondsField.setText(twoDigitFormat.format((millisUntilFinished / 1000)%60));
                    //milisecondsLeft = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    onTimerFinish();
                }
            }.start();
            myTimerState = TimerState.RUNNING;
        }
    }

    private void pauseTimer() {
        if (cdTimer != null) {//check if there has been something before`
            cdTimer.cancel();
            cdTimer = null;
            //should be more accurate than using the value from onTick();
            milisecondsLeft = calculateTimeToGo();
            Log.v("P1 pauseT()  ", String.valueOf(milisecondsLeft));
            myTimerState = TimerState.PAUSED;
        }
    }

    private void resumeTimer() {
        myPreferences.setStartedTime(getNow());
        lastSetTimeMillis = milisecondsLeft;
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

    private long getNow() {
        //get a callendar object to get current time
        Calendar rightNow = Calendar.getInstance();
        //return the current time in seconds
        return rightNow.getTimeInMillis();
    }

    private void setAlarm() {
        //the wake up time is calculated by adding the set running time to the start time, which
        //was saved in preferences
        long wakeUpTime = (myPreferences.getStartedTime() + lastSetTimeMillis);
        //getting the AlarmService instance wich allows the end of timer notification be used
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //the intent opens the TimerExpiredClass wich sets the notification
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        //Setting up the pendingIntent wich will be launched when the time ends
        //FLAG_CANCEL_CURRENT does not allow creating duplicate pendingIntents
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //set the alarm with the time and pending intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, sender), sender);
        } else {
            //Beginning in API 19, the trigger time passed to this method is treated as inexact (for set)
            //maybe that is why setAlarmClockIsUsedBefore
            am.set(AlarmManager.RTC_WAKEUP, wakeUpTime, sender);
        }

    }

    private void removeAlarm() {
        //using the same steps like the set alarm, but instead we cance at the end
        //will be used when we enter the app and the timer is running
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    private void initTimerAfterAppPause() {
        //get the start time of the timer from preferences
        long startTime = myPreferences.getStartedTime();
        Log.v("P2 starttime()  ", String.valueOf(startTime));
        //check if there has been a timer, we set the default value to be 0  in MyPreferencesClass
        if (startTime > 0) {
            //calculate the time left on the timer
            long timeToGo = calculateTimeToGo();
            Log.v("P2 timetogo()  ", String.valueOf(timeToGo));
            if (timeToGo <= 0) {
                //timer has already expired
                onTimerFinish();
            } else {
                startTimer(timeToGo);
            }
        }

    }

    private void onTimerFinish() {
        //set the value in preferences to zero
        myPreferences.setStartedTime(0);

        minutesField.setText(R.string.time_on_finish);
        secondsField.setText(R.string.time_on_finish);
        startPauseResumeButton.setText(R.string.start);
        cdTimer = null;
        myTimerState = TimerState.STOPPED;


        //Sound effect
        releaseMediaPlayer();
        int result = myAudioManager.requestAudioFocus(myOnAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            myMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.end);
            myMediaPlayer.start();
            myMediaPlayer.setOnCompletionListener(myOnCompletionListener);
        }
    }

    private void updateStartPauseResumeButtonText() {
        if (myTimerState == TimerState.RUNNING) {
            startPauseResumeButton.setText(R.string.pause);
        } else if (myTimerState == TimerState.PAUSED) {
            startPauseResumeButton.setText(R.string.resume);
        } else {
            startPauseResumeButton.setText(R.string.start);
        }
    }

    private long calculateTimeToGo() {//calculate time to go on a running timer
        long startTime = myPreferences.getStartedTime();
        return (lastSetTimeMillis - (getNow() - startTime));
    }

    private void updateTimeUI(long time) {
        minutesField.setText(twoDigitFormat.format(time / 1000 / 60));
        secondsField.setText(twoDigitFormat.format((time / 1000) % 60));
    }

    private enum TimerState {
        RUNNING,
        PAUSED,
        STOPPED
    }
}
