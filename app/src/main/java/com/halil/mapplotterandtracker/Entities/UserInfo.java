package com.halil.mapplotterandtracker.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "userinfo_table")

public class UserInfo implements Serializable {
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "userinfoID") public int mUserinfoID;

    @ColumnInfo(name = "mName") public String mName;

    @ColumnInfo(name = "mBirthYear") public int mBirthYear;

    @ColumnInfo(name = "mWeight") public int mWeight;

    @ColumnInfo(name = "mTotalDistanceHiked") public double mTotalDistanceHiked;

    @ColumnInfo(name = "mTotalToughness") public double mTotalToughness;

    public UserInfo(int mUserinfoID, String mName, int mBirthYear, int mWeight, double mTotalDistanceHiked,
                    double mTotalToughness) {
        this.mUserinfoID = mUserinfoID;
        this.mName = mName;
        this.mBirthYear = mBirthYear;
        this.mWeight = mWeight;
        this.mTotalDistanceHiked = mTotalDistanceHiked;
        this.mTotalToughness = mTotalToughness;
    }

    public int getmUserinfoID() { return mUserinfoID; }
    public String getmName() { return mName; }
    public double getmBirthYear() { return mBirthYear; }
    public double getmWeight() { return mWeight; }
    public double getmTotalDistanceHiked() { return mTotalDistanceHiked; }
    public double getmTotalToughness() { return mTotalToughness; }

    public void setmUserinfoID(int mUserinfoID) { this.mUserinfoID = mUserinfoID; }
    public void setmName(String mName) { this.mName = mName; }
    public void setmBirthYear(int mBirthYear) { this.mBirthYear = mBirthYear; }
    public void setmWeight(int mWeight) { this.mWeight = mWeight; }
    public void setmTotalDistanceHiked(double mTotalDistanceHiked) {
        this.mTotalDistanceHiked = mTotalDistanceHiked; }
    public void setmTotalToughness(double mTotalToughness) {
        this.mTotalToughness = mTotalToughness; }
}
