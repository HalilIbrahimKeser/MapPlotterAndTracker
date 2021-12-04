package com.halil.mapplotterandtracker.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;
import com.halil.mapplotterandtracker.db.Dao;
import com.halil.mapplotterandtracker.db.RoomDatabase;

import java.util.List;

public class Repository {

    private final Dao mDao;
    private final LiveData<List<Trip>> mAllTrips;
    private final LiveData<List<Locations>> mAllLocations;
    public LiveData<List<Trip>> singleTrip;
    public LiveData<List<UserInfo>> user;

    public Repository(Application application) {

        RoomDatabase db = RoomDatabase.getDatabase(application);
        mDao = db.Dao();
        mAllTrips = mDao.getTrips();
        mAllLocations = mDao.getAllLocations();
    }

    /** USER --------------------*/
    public void userInfoInsert(UserInfo user) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.userInfoInsert(user);
        });
    }

    public LiveData<List<UserInfo>> getUser(int userinfoID) {
        user = mDao.getUser(userinfoID);
        return user;
    }

    public void userInfoUpdate(String nameString, double etAgeInt, double etWeightInt ) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.userInfoUpdate(nameString, etAgeInt, etWeightInt);
        });
    }

    public void updateUser(UserInfo user) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.updateUser(user);
        });
    }

    public void deleteUser(int userID) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.deleteUser(userID);
        });
    }

    /** LOCATION --------------------*/
    public void locationInsert(Locations location) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.locationInsert(location);
        });
    }

    public LiveData<List<Locations>> getAllLocations() {
        LiveData<List<Locations>> mAllLocations1 = mDao.getAllLocations();
        return mAllLocations1;
    }

    public void deleteAllLocations(List<Locations> locationsList) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.resetLocations(locationsList);
        });
    }

    /** TRIP --------------------*/
    public void tripInsert(Trip trip) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.tripInsert(trip);
        });
    }

    public LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    }

    public LiveData<Trip> getTrip(int mTripID) {
        return mDao.getTrip(mTripID);
    }

    public LiveData<List<Trip>> getSingleTrip(int mTripID) {
        singleTrip = mDao.getSingleTrip(mTripID);
        return singleTrip;
    }

    public void deleteTrip(int trip) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.deleteTrip(trip);
        });
    }
}
