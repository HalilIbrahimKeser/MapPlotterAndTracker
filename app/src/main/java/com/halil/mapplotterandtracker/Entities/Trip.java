package com.halil.mapplotterandtracker.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "trip_table")
public class Trip implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "mTripId") public int mTripId;

    @ColumnInfo(name = "mFromAddress") public String mFromAddress;

    @ColumnInfo(name = "mToAddress") public String mToAddress;

    @ColumnInfo(name = "mLength") public double mLength;

    @ColumnInfo(name = "mNodes") public double mNodes;

    @ColumnInfo(name = "mDuration") public double mDuration;

    @ColumnInfo(name = "mElevation") public double mElevation;

    @ColumnInfo(name = "mStartPointLat") public double mStartPointLat;

    @ColumnInfo(name = "mStartPointLong") public double mStartPointLong;

    @ColumnInfo(name = "mEndPointLat") public double mEndPointLat;

    @ColumnInfo(name = "mEndPointLong") public double mEndPointLong;

    public Trip(String mFromAddress, String mToAddress,
                double mLength, double mNodes,
                double mDuration, double mElevation,
                double mStartPointLat, double mStartPointLong,
                double mEndPointLat, double mEndPointLong) {
        this.mFromAddress = mFromAddress;
        this.mToAddress = mToAddress;
        this.mLength = mLength;
        this.mNodes = mNodes;
        this.mDuration = mDuration;
        this.mElevation = mElevation;
        this.mStartPointLat = mStartPointLat;
        this.mStartPointLong = mStartPointLong;
        this.mEndPointLat = mEndPointLat;
        this.mEndPointLong = mEndPointLong;
    }

    public int getmTripId() { return mTripId; }
    public double getmLength() { return mLength; }
    public double getmNodes() { return mNodes; }
    public double getmDuration() { return mDuration; }
    public double getmElevation() { return mElevation; }
    public String getmFromAddress() { return mFromAddress; }
    public String getmToAddress() { return mToAddress; }
    public double getmStartPointLat() { return mStartPointLat; }
    public double getmStartPointLong() { return mStartPointLong; }
    public double getmEndPointLat() { return mEndPointLat; }
    public double getMmEndPointLong() { return mEndPointLong; }

    public void setmTripId(int mTripId) { this.mTripId = mTripId; }
    public void setmLength(double mLength) { this.mLength = mLength; }
    public void setmNodes(double mNodes) { this.mNodes = mNodes; }
    public void setmDuration(double mDuration) { this.mDuration = mDuration; }
    public void setmElevation(double mElevation) { this.mElevation = mElevation; }
    public void setmFromAddress(String mFromAddress) { this.mFromAddress = mFromAddress; }
    public void setmToAddress(String mToAddress) { this.mToAddress = mToAddress; }
    public void setmStartPointLat(double mStartPointLat) { this.mStartPointLat = mStartPointLat; }
    public void setmStartPointLong(double mStartPointLong) { this.mStartPointLong = mStartPointLong; }
    public void setmEndPointLat(double mEndPointLat) { this.mEndPointLat = mEndPointLat; }
    public void setMmEndPointLong(double mmEndPointLong) { this.mEndPointLong = mmEndPointLong; }
}
