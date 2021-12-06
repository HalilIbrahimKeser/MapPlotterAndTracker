package com.halil.mapplotterandtracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;


import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;

import java.util.List;

@androidx.room.Dao
public interface Dao {

    // LOCATION - - - - - - -
    @Delete
    void resetLocations(List<Locations> locationsList);

    @Query("DELETE FROM location_table")
    void resetAllLocations();

    @Query("SELECT * FROM location_table ORDER BY locationID")
    LiveData<List<Locations>> getAllLocations();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void locationInsert(Locations location);

//    @Query("SELECT * FROM location_table WHERE locTripID = :mTripID")
//    LiveData<List<Locations>> getLocationPath(int mTripID);

    // USER - - - - - - - - - - - - - - - - - - - - -  -
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void userInfoInsert(UserInfo user);

    @Query("UPDATE userinfo_table SET mName=:nameString, mBirthYear=:etAgeInt, mWeight=:etWeightInt WHERE userinfoID = 1")
    void userInfoUpdate(String nameString, double etAgeInt, double etWeightInt);

    @Update
    void updateUser(UserInfo user);

    @Query("DELETE FROM userinfo_table WHERE userinfoID = :userinfoID")
    void deleteUser(int userinfoID);

    @Query("SELECT * FROM userinfo_table WHERE userinfoID = :userinfoID")
    LiveData<List<UserInfo>> getUser(int userinfoID);

    // TRIP - - - - - - - - - - - - - - - - - - - - -  -
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void tripInsert(Trip trip);

    @Query("SELECT * FROM trip_table WHERE isFinished == 1 ORDER BY mTripId")
    LiveData<List<Trip>> getFinnishedTrips();
    @Query("SELECT * FROM trip_table WHERE isFinished == 0 ORDER BY mTripId")
    LiveData<List<Trip>> getNotFinnishedTrips();


    @Query("SELECT * FROM trip_table WHERE mTripId = :mTripId")
    LiveData<List<Trip>> getTripData(int mTripId);

    @Query("DELETE FROM trip_table")
    void deleteAlltrips();

    @Query("SELECT * FROM trip_table WHERE mTripId = :mTripId")
    LiveData<Trip> getTrip(int mTripId);

    @Query("DELETE FROM trip_table WHERE mTripId = :mTripID")
    void deleteTrip(int mTripID);

    @Query("SELECT * FROM trip_table WHERE mTripID = :mTripID")
    LiveData<List<Trip>> getSingleTrip(int mTripID);


}
