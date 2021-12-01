package com.halil.mapplotterandtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.databinding.ActivityMainBinding;
import com.halil.mapplotterandtracker.databinding.ActivitySettingsBinding;

import org.osmdroid.config.Configuration;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    public Context context;

    Button btSavePerson;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivitySettingsBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Toolbar
        Toolbar myToolbar = binding.myToolbar4;
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Buttom Navigation
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.userSettings);
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

        btSavePerson = binding.btSavePerson;
        ///binding.

    }
}