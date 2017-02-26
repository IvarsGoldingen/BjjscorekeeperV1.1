package com.example.android.bjjscorekeeper;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ivars on 2017.01.28..
 */

public class ScoreAdapter extends ArrayAdapter<Score> {
    public ScoreAdapter(Activity context, ArrayList<Score> scores){
        super(context, 0, scores);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItemView = convertView;
        if(listItemView==null){
            listItemView= LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        Score currentScore=getItem(position);
        TextView ScoreNumberView = (TextView) listItemView.findViewById(R.id.score_text_view);
        ScoreNumberView.setText(String.valueOf(currentScore.GetScoreNumber()));

        String cornerScored = currentScore.GetCornerScored();
        TextView CornerScoredNumberView = (TextView) listItemView.findViewById(R.id.corner_scored_text_view);
        CornerScoredNumberView.setText(cornerScored);

        TextView PointsScoredView = (TextView) listItemView.findViewById(R.id.points_scored_text_view);
        PointsScoredView.setText(currentScore.GetPointsScored());

        View containerView = (View)listItemView.findViewById(R.id.containerView);
        String redTeam = getContext().getString(R.string.red_corner);
        if(cornerScored.equals(redTeam) ){
            containerView.setBackgroundColor(getContext().getColor(R.color.redColor));
        }
        else{
            containerView.setBackgroundColor(getContext().getColor(R.color.blueColor));
        }
        //getResources().getString(R.string.blue_corner)
        return listItemView;
    }

}
