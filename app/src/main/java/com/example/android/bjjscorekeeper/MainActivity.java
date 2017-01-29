package com.example.android.bjjscorekeeper;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.android.bjjscorekeeper.R.string.history;

public class MainActivity extends AppCompatActivity {
    int redPoints=0;
    int redAdvantages=0;
    int bluePoints=0;
    int blueAdvantages=0;
    int scoreNumber=1;
    ArrayList<Score> scores = new ArrayList<Score>();

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {       //restore value
        redPoints = savedInstanceState.getInt("redPointsBackup");
        redAdvantages = savedInstanceState.getInt("redAdvantagesBackup");
        bluePoints = savedInstanceState.getInt("bluePointsBackup");
        blueAdvantages = savedInstanceState.getInt("blueAdvantagesBackup");
        super.onRestoreInstanceState(savedInstanceState);
        refreshAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {                    // save varible to temporary varible
        super.onSaveInstanceState(outState);
        outState.putInt("redPointsBackup",redPoints);
        outState.putInt("redAdvantagesBackup",redAdvantages);
        outState.putInt("bluePointsBackup",bluePoints);
        outState.putInt("blueAdvantagesBackup",blueAdvantages);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button bluePoints4 = (Button)findViewById(R.id.blue_4points);
        bluePoints4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points4)));
                bluePoints+=4;
                refreshPointsBlue();
            }
        });

        Button bluePoints3 = (Button)findViewById(R.id.blue_3points);
        bluePoints3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points3)));
                bluePoints+=3;
                refreshPointsBlue();
            }
        });

        Button bluePoints2 = (Button)findViewById(R.id.blue_2points);
        bluePoints2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.points2)));
                bluePoints+=2;
                refreshPointsBlue();
            }
        });

        final Button blueAdvantage = (Button)findViewById(R.id.blue_advantage);
        blueAdvantage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.blue_corner),getResources().getString(R.string.advantage)));
                blueAdvantages+=1;
                refreshAdvantagesBlue();
            }
        });

        Button redPoints4 = (Button)findViewById(R.id.red_4points);
        redPoints4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points4)));
                redPoints+=4;
                refreshPointsRed();
            }
        });

        Button redPoints3 = (Button)findViewById(R.id.red_3points);
        redPoints3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points3)));
                redPoints+=3;
                refreshPointsRed();
            }
        });

        Button redPoints2 = (Button)findViewById(R.id.red_2points);
        redPoints2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.points2)));
                redPoints+=2;
                refreshPointsRed();
            }
        });

        final Button redAdvantage = (Button)findViewById(R.id.red_advantage);
        redAdvantage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scores.add(new Score(scoreNumber++,getResources().getString(R.string.red_corner),getResources().getString(R.string.advantage)));
                redAdvantages+=1;
                refreshAdvantagesRed();
            }
        });

        Button resetButton = (Button)findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluePoints=0;
                redPoints=0;
                blueAdvantages=0;
                redAdvantages=0;
                scoreNumber=1;
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
    void refreshAll(){
        refreshPointsRed();
        refreshPointsBlue();
        refreshAdvantagesBlue();
        refreshAdvantagesRed();
    }
    void refreshPointsBlue(){
        TextView bluePointsView = (TextView)findViewById(R.id.blue_points_view);
        bluePointsView.setText(String.valueOf(bluePoints));
    }
    void refreshPointsRed(){
        TextView redPointsView = (TextView)findViewById(R.id.red_points_view);
        redPointsView.setText(String.valueOf(redPoints));
    }
    void refreshAdvantagesBlue(){
        TextView blueAdvantagesView = (TextView)findViewById(R.id.blue_advantages_view);
        blueAdvantagesView.setText(String.valueOf(blueAdvantages));
    }
    void refreshAdvantagesRed(){
        TextView redAdvantagesView = (TextView)findViewById(R.id.red_advantages_view);
        redAdvantagesView.setText(String.valueOf(redAdvantages));
    }
}
