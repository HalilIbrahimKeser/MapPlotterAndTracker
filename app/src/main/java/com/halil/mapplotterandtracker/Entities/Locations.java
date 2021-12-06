package com.halil.mapplotterandtracker.Entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "location_table",
        indices = {@Index("mLocationTripId")},
        foreignKeys = {@ForeignKey(entity = Trip.class,
                parentColumns = "mTripId",
                childColumns = "mLocationTripId",
                onDelete = CASCADE)})

public class Locations implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "locationID") public int mLocationID;

    @ColumnInfo(name = "mLocationTripId") public int mLocationTripId;

    @ColumnInfo(name = "mLatitude") public double mLatitude;

    @ColumnInfo(name = "mLongitude") public double mLongitude;

    @ColumnInfo(name = "mAltitude") public double mAltitude;

    @ColumnInfo(name = "mEasting") public double mEasting;

    @ColumnInfo(name = "mNorthing") public double mNorthing;

    @ColumnInfo(name = "mLetter") public char mLetter;

    @ColumnInfo(name = "mZone") public double mZone;

    @ColumnInfo(name = "mBearing") public double mBearing;

    @ColumnInfo(name = "mBearingAccuracyDegrees") public double mBearingAccuracyDegrees;

    public Locations(int mLocationTripId, double mLatitude, double mLongitude, double mAltitude, double mEasting,
                     double mNorthing, char mLetter, double mZone, double mBearing, double mBearingAccuracyDegrees) {
        this.mLocationTripId = mLocationTripId;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mEasting = mEasting;
        this.mAltitude = mAltitude;
        this.mNorthing = mNorthing;
        this.mLetter = mLetter;
        this.mZone = mZone;
        this.mBearing = mBearing;
        this.mBearingAccuracyDegrees = mBearingAccuracyDegrees;
    }

    public int getmLocationID() { return mLocationID; }
    public double getmLocationTripId() {
        return mLocationTripId;
    }
    public double getmLatitude() {
        return mLatitude;
    }
    public double getmLongitude() {
        return mLongitude;
    }
    public double getmAltitude() {
        return mAltitude;
    }
    public double getmEasting() { return mEasting; }
    public double getmNorthing() { return mNorthing; }
    public char getmLetter() { return mLetter; }
    public double getmZone() { return mZone; }
    public double getmBearing() { return mBearing; }
    public double getmBearingAccuracyDegrees() { return mBearingAccuracyDegrees; }

    public void setmLocationID(int mLocationID) { this.mLocationID = mLocationID; }
    public void setmLocationTripId(int mLocationTripId) { this.mLocationTripId = mLocationTripId; }
    public void setmZone(double zone) { this.mZone = zone; }
    public void setmLatitude(double mLatitude) { this.mLatitude = mLatitude; }
    public void setmLongitude(double mLongitude) { this.mLongitude = mLongitude; }
    public void setmAltitude(double mAltitude) { this.mAltitude = mAltitude; }
    public void setmLetter(char mLetter) { this.mLetter = mLetter; }
    public void setmEasting(double mEasting) { this.mEasting = mEasting; }
    public void setmNorthing(double mNorthing) { this.mNorthing = mNorthing; }
    public void setmBearing(double mBearing) { this.mBearing = mBearing; }
    public void setmBearingAccuracyDegrees(double mBearingAccuracyDegrees) { this.mBearingAccuracyDegrees = mBearingAccuracyDegrees; }
}
