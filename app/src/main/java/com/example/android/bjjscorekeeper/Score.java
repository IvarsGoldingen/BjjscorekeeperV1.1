package com.example.android.bjjscorekeeper;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Ivars on 2017.01.28..
 */

public class Score implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Score> CREATOR = new Parcelable.Creator<Score>() {
        @Override
        public Score createFromParcel(Parcel in) {
            return new Score(in);
        }

        @Override
        public Score[] newArray(int size) {
            return new Score[size];
        }
    };
    private int mScoreNumber;
    private String mCornerScored;
    private String mPointsScored;

    public Score(int scoreNumber, String cornerScored, String pointsScored){
        mScoreNumber=scoreNumber;
        mCornerScored=cornerScored;
        mPointsScored=pointsScored;
    }

    private Score(Parcel in) {
        mScoreNumber = in.readInt();
        mCornerScored = in.readString();
        mPointsScored = in.readString();
    }

    public int GetScoreNumber(){return mScoreNumber;}

    public String GetCornerScored(){
        return mCornerScored;
    }

    public String GetPointsScored(){
        return mPointsScored;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mScoreNumber);
        dest.writeString(mCornerScored);
        dest.writeString(mPointsScored);
    }
}