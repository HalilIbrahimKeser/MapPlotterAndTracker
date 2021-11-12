package com.halil.mapplotterandtracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;


import com.halil.mapplotterandtracker.Entities.Trip;

import java.util.List;

@androidx.room.Dao
public interface Dao {

    //TRIP - - - - - - - - - - - - - - - - - - - - -  -
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void tripInsert(Trip trip);

    @Query("SELECT * FROM trip_table ORDER BY mTripId")
    LiveData<List<Trip>> getTrips();

    @Query("SELECT * FROM trip_table WHERE mTripId = :mTripId")
    LiveData<List<Trip>> getTripData(int mTripId);

    @Query("DELETE FROM trip_table")
    void deleteAlltrips();

    @Query("SELECT * FROM trip_table WHERE mTripId = :mTripId")
    LiveData<Trip> getTrip(int mTripId);

    @Query("DELETE FROM trip_table WHERE mTripId = :mTripID")
    void deleteTrip(int mTripID);

}
