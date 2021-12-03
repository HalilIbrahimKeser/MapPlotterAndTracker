package com.halil.mapplotterandtracker.VievModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;
import com.halil.mapplotterandtracker.Repository.Repository;
import java.util.List;

public class ViewModel extends AndroidViewModel {

    private final Repository mRepository;
    public LiveData<List<Trip>> mAllTrips;
    public LiveData<List<Locations>> mAllLocations;
    public LiveData<Trip> mCurrentTrip;
    public MutableLiveData<Trip> mCurentHiking;
    public Trip mTrip;
    public LiveData<List<UserInfo>> mUser;

    public ViewModel(Application application) {
        super(application);
        this.mRepository = new Repository(application);
        mAllTrips = mRepository.getAllTrips();
        mAllLocations = mRepository.getAllLocations();
    }

    // User
    public LiveData<List<UserInfo>> getSingleUser(int userinfoID) {
        mUser = mRepository.getUser(userinfoID);
        return mUser;
    }

    // Locations
    public LiveData<List<Locations>> getAllLocations() {
        return mAllLocations;
    }

    public void insertLocation(Locations location) {
        mRepository.locationInsert(location);
    }

    //TRIP ------------------------------------------------------------
    public void insertTrip(Trip trip) {
        mRepository.tripInsert(trip);
    }

    public MutableLiveData<Trip> getCurrentTrip() {
        if (mCurentHiking == null) {
            mCurentHiking = new MutableLiveData<>();
        }
        return mCurentHiking;
    }

    public LiveData<List<Trip>> getSingleTrip(int mTripID) {
        mTrip = mRepository.getSingleTrip(mTripID).getValue().get(0);
        return mRepository.getSingleTrip(mTripID);
    }

    public LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    }

    public LiveData<Trip> getTrip(int mTripId) {
        return mRepository.getTrip(mTripId);
    }
}
