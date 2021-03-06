package com.example.android.bjjscorekeeper;

import android.app.Activity;
import android.os.Build;
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




        TextView PointsScoredView = (TextView) listItemView.findViewById(R.id.points_scored_text_view);
        PointsScoredView.setText(currentScore.GetPointsScored());

        View containerView = (View)listItemView.findViewById(R.id.containerView);
        String redTeam = getContext().getString(R.string.red_corner);

        String cornerScored = currentScore.GetCornerScored();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cornerScored.equals(redTeam)) {
                containerView.setBackgroundColor(getContext().getColor(R.color.redColor));
            } else {
                containerView.setBackgroundColor(getContext().getColor(R.color.blueColor));
            }
        }
        else{
            //if the container's color can't be set because of older SDK, use text
            TextView CornerScoredNumberView = (TextView) listItemView.findViewById(R.id.corner_scored_text_view);
            CornerScoredNumberView.setText(cornerScored);
        }
        //getResources().getString(R.string.blue_corner)
        return listItemView;
    }

}
