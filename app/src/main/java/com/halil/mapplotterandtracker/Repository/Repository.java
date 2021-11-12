package com.halil.mapplotterandtracker.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.db.Dao;
import com.halil.mapplotterandtracker.db.RoomDatabase;

import java.util.List;

public class Repository {

    private final Dao mDao;
    private final LiveData<List<Trip>> mAllTrips;

    public Repository(Application application) {

        RoomDatabase db = RoomDatabase.getDatabase(application);
        mDao = db.Dao();
        mAllTrips = mDao.getTrips();
    }

    //TRIP--------------------
    public void tripInsert(Trip trip) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.tripInsert(trip);
        });
    }

    public LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    }

    public LiveData<Trip> getTrip(int mTripID) {
        LiveData<Trip> trip = mDao.getTrip(mTripID);
        return trip;
    }

    public void deleteTrip(int trip) {
        RoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.deleteTrip(trip);
        });
    }
}
