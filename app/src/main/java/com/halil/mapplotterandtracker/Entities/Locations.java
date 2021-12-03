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

    @ColumnInfo(name = "mEasting") public double mEasting;

    @ColumnInfo(name = "mNorthing") public double mNorthing;

    @ColumnInfo(name = "mLetter") public char mLetter;

    @ColumnInfo(name = "Zone") public double Zone;

    public Locations(int mLocationTripId, double mLatitude, double mLongitude, double mEasting, double mNorthing, char mLetter, double Zone ) {
        this.mLocationTripId = mLocationTripId;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mEasting = mEasting;
        this.mNorthing = mNorthing;
        this.mLetter = mLetter;
        this.Zone = Zone;
    }
    public double getmLocationTripId() {
        return mLocationTripId;
    }
    public double getmLatitude() {
        return mLatitude;
    }
    public double getmLongitude() {
        return mLongitude;
    }
    public double getmEasting() { return mEasting; }
    public double getmNorthing() { return mNorthing; }
    public char getmLetter() { return mLetter; }
    public double getZone() { return Zone; }

    public void setmLocationTripId(int mLocationTripId) { mLocationTripId = mLocationTripId; }
    public void setZone(double zone) { Zone = zone; }
    public void setmLatitude(double mLatitude) { this.mLatitude = mLatitude; }
    public void setmLongitude(double mLongitude) { this.mLongitude = mLongitude; }
    public void setmLetter(char mLetter) { this.mLetter = mLetter; }
    public void setmEasting(double mEasting) { this.mEasting = mEasting; }
    public void setmNorthing(double mNorthing) { this.mNorthing = mNorthing; }
}
