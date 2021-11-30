package com.halil.mapplotterandtracker.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

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

    @ColumnInfo(name = "mDistance") public double mDistance;

    @ColumnInfo(name = "mElevation") public double mElevation;

    @ColumnInfo(name = "isFinished") public Boolean mIsFinished;

    @Embedded public StartGeo startGeo;

    @Embedded public StopGeo stopGeo;

    public Trip(String mFromAddress, String mToAddress,
                double mLength, double mNodes,
                double mDuration, double mDistance, double mElevation,
                StartGeo startGeo, StopGeo stopGeo) {
        this.mFromAddress = mFromAddress;
        this.mToAddress = mToAddress;
        this.mLength = mLength;
        this.mNodes = mNodes;
        this.mDuration = mDuration;
        this.mDistance = mDistance;
        this.mElevation = mElevation;
        this.startGeo = startGeo;
        this.stopGeo = stopGeo;
    }

    public static class StartGeo {
        public double mStartPointLat;
        public double mStartPointLong;

        public StartGeo(double mStartPointLat, double mStartPointLong) {
            this.mStartPointLat = mStartPointLat;
            this.mStartPointLong = mStartPointLong;
        }

        public double getmStartPointLat() { return mStartPointLat; }
        public void setmStartPointLat(double mStartPointLat) { this.mStartPointLat = mStartPointLat; }
        public double getmStartPointLong() { return mStartPointLong; }
        public void setmStartPointLong(double mStartPointLong) { this.mStartPointLong = mStartPointLong; }
    }

    public static class StopGeo {
        public double mEndPointLat;
        public double mEndPointLong;

        public StopGeo(double mEndPointLat, double mEndPointLong) {
            this.mEndPointLat = mEndPointLat;
            this.mEndPointLong = mEndPointLong;
        }

        public double getmEndPointLat() { return mEndPointLat; }
        public void setmEndPointLat(double mEndPointLat) { this.mEndPointLat = mEndPointLat; }
        public double getmEndPointLong() { return mEndPointLong; }
        public void setmEndPointLong(double mEndPointLong) { this.mEndPointLong = mEndPointLong; }
    }

    public StartGeo getStartGeo() { return startGeo; }
    public void setStartGeo(StartGeo startGeo) { this.startGeo = startGeo; }

    public StopGeo getStopGeo() { return stopGeo; }
    public void setStopGeo(StopGeo stopGeo) { this.stopGeo = stopGeo; }

    public int getmTripId() { return mTripId; }
    public double getmLength() { return mLength; }
    public double getmNodes() { return mNodes; }
    public double getmDuration() { return mDuration; }
    public double getmDistance() { return mDistance; }
    public double getmElevation() { return mElevation; }
    public String getmFromAddress() { return mFromAddress; }
    public String getmToAddress() { return mToAddress; }
    public Boolean getmIsFinished() { return mIsFinished; }

    public void setmTripId(int mTripId) { this.mTripId = mTripId; }
    public void setmLength(double mLength) { this.mLength = mLength; }
    public void setmNodes(double mNodes) { this.mNodes = mNodes; }
    public void setmDuration(double mDuration) { this.mDuration = mDuration; }
    public void setmDistance(double mDistance) { this.mDistance = mDistance; }
    public void setmElevation(double mElevation) { this.mElevation = mElevation; }
    public void setmFromAddress(String mFromAddress) { this.mFromAddress = mFromAddress; }
    public void setmToAddress(String mToAddress) { this.mToAddress = mToAddress; }
    public void setmIsFinished(Boolean mIsFinished) { this.mIsFinished = mIsFinished; }
}
