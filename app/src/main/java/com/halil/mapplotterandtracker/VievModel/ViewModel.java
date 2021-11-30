package com.halil.mapplotterandtracker.VievModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Repository.Repository;
import java.util.List;

public class ViewModel extends AndroidViewModel {

    private final Repository mRepository;
    public LiveData<List<Trip>> mAllTrips;
    public LiveData<Trip> mCurrentTrip;
    public MutableLiveData<Trip> mCurentHiking;
    public Trip mTrip;

    public ViewModel(Application application) {
        super(application);
        this.mRepository = new Repository(application);
        mAllTrips = mRepository.getAllTrips();
    }

    //TRIP ------------------------------------------------------------
    public void insertTrip(Trip trip) {
        mRepository.tripInsert(trip);
    }

    public LiveData<Trip> getCurrentTrip() {
        if (mCurrentTrip == null) {
            mCurrentTrip = new MutableLiveData<>();
        }
        return mCurrentTrip;
    }

    public Repository getmRepository() { return mRepository; }

    public LiveData<List<Trip>> getmAllTrips() { return mAllTrips; }

    public void setmAllTrips(LiveData<List<Trip>> mAllTrips) { this.mAllTrips = mAllTrips; }

    public LiveData<Trip> getmCurrentTrip() { return mCurrentTrip; }

    public void setmCurrentTrip(LiveData<Trip> mCurrentTrip) { this.mCurrentTrip = mCurrentTrip; }

    public MutableLiveData<Trip> getmCurentHiking() { return mCurentHiking; }

    public void setmCurentHiking(MutableLiveData<Trip> mCurentHiking) { this.mCurentHiking = mCurentHiking; }

    public Trip getmTrip() { return mTrip; }

    public void setmTrip(Trip mTrip) { this.mTrip = mTrip; }

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
