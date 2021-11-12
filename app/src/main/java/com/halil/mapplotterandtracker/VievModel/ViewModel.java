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

    public LiveData<List<Trip>> getAllTrips() {
        return mAllTrips;
    }

    public LiveData<Trip> getTrip(int mTripId) {
        this.mCurrentTrip = mRepository.getTrip(mTripId);
        return  this.mCurrentTrip;
    }

}
