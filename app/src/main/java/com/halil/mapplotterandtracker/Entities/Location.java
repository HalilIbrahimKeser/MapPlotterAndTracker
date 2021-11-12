package com.halil.mapplotterandtracker.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "location_table")
public class Location implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "locationID") public int mLocationID;

    @ColumnInfo(name = "mLatitude") public double mLatitude;

    @ColumnInfo(name = "mLongitude") public double mLongitude;

    @ColumnInfo(name = "mEasting") public double mEasting;

    @ColumnInfo(name = "mNorthing") public double mNorthing;

    @ColumnInfo(name = "mLetter") public String mLetter;

    @ColumnInfo(name = "Zone") public double Zone;

    public Location(double mLatitude, double mLongitude, double mEasting, double mNorthing, String mLetter, double Zone ) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mEasting = mEasting;
        this.mNorthing = mNorthing;
        this.mLetter = mLetter;
        this.Zone = Zone;
    }
    public double getmLatitude() {
        return mLatitude;
    }
    public double getmLongitude() {
        return mLongitude;
    }
    public double getmEasting() { return mEasting; }
    public double getmNorthing() { return mNorthing; }
    public String getmLetter() { return mLetter; }
    public double getZone() { return Zone; }

    public void setZone(double zone) { Zone = zone; }
    public void setmLatitude(double mLatitude) { this.mLatitude = mLatitude; }
    public void setmLongitude(double mLongitude) { this.mLongitude = mLongitude; }
    public void setmLetter(String mLetter) { this.mLetter = mLetter; }
    public void setmEasting(double mEasting) { this.mEasting = mEasting; }
    public void setmNorthing(double mNorthing) { this.mNorthing = mNorthing; }
}
