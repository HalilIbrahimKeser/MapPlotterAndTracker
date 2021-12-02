package com.halil.mapplotterandtracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.Adapter.AdapterClass;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.VievModel.ViewModel;
import com.halil.mapplotterandtracker.databinding.ActivityPlanedRoutesBinding;

import org.osmdroid.config.Configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlanedRoutesActivity extends AppCompatActivity {

    ActivityPlanedRoutesBinding binding;
    Intent intent;
    Context context;
    RecyclerView recyclerView;
    ViewModel mViewModel;
    AdapterClass adapter;
    Trip mCurentHiking;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityPlanedRoutesBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Toolbar
        Toolbar myToolbar = binding.myToolbar3;
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Buttom Navigation
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.plannedTrips);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            switch (item.getItemId()) {
                case R.id.main:
                    intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.plannedTrips:
                    intent = new Intent(context, PlanedRoutesActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.recordedTrips:
                    intent = new Intent(context, RecordedTripsActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.userSettings:
                    intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        });

        // Recyclerview
        recyclerView = binding.recyclerview;

        // ViewModel
        mViewModel = new ViewModelProvider(this).get(ViewModel.class);
        mViewModel.getAllTrips().observe(this, trips -> { /////
            adapter = new AdapterClass(this, trips);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter.setClickListener((view, position) -> {
                mCurentHiking = trips.get(position);
                mViewModel.getCurrentTrip().postValue(mCurentHiking);

                Intent intent = new Intent(context, MainActivity.class);
                Bundle args = new Bundle();

                List<Trip> list = new ArrayList<Trip>();
                list.add(mCurentHiking);

                args.putSerializable("arraylist", (Serializable) list);
                intent.putExtra("bundle", args);
                startActivity(intent);
            });
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        intent = new Intent(this, MainActivity.class);
        switch (item.getItemId()) {
            case R.id.menu_goback:
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.top_menu2, menu);
        return true;
    }
}