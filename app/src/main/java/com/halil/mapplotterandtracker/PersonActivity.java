package com.halil.mapplotterandtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.Entities.UserInfo;
import com.halil.mapplotterandtracker.Repository.Repository;
import com.halil.mapplotterandtracker.VievModel.ViewModel;
import com.halil.mapplotterandtracker.databinding.ActivityPersonBinding;
import com.halil.mapplotterandtracker.databinding.ActivitySettingsBinding;

import org.osmdroid.config.Configuration;

import java.util.List;
import java.util.Objects;

public class PersonActivity extends AppCompatActivity {

    ActivityPersonBinding binding;
    public Context context;

    TextView tvName;
    TextView tvAge;
    TextView tvWeight;
    TextView tvTotalDistanceHiked;
    TextView tvTotalToughness;
    TextView tvAverageToughness;

    private Repository mRepository;
    private ViewModel mViewModel;
    public List<UserInfo> mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityPersonBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Repo
        mRepository = new Repository(getApplication());

        tvName = binding.tvName1;
        tvAge = binding.tvAge1;
        tvWeight = binding.tvWeight1;
        tvTotalDistanceHiked = binding.tvTotalDistanceHiked1;
        tvTotalToughness = binding.tvTotalToughness1;
        tvAverageToughness = binding.tvAverageToughness1;

        // View model
        mViewModel = new ViewModelProvider(this).get(ViewModel.class);
        mViewModel.getSingleUser(1).observe(this, user -> {
            mUser = user;
            tvName.setText(mUser.get(0).mName);
            tvAge.setText(String.valueOf(mUser.get(0).mBirthYear));
            tvWeight.setText(String.valueOf(mUser.get(0).mWeight));
            tvTotalDistanceHiked.setText((int) mUser.get(0).mTotalDistanceHiked);
            tvTotalToughness.setText((int) mUser.get(0).mTotalToughness);
            //tvAverageToughness.setText(String.valueOf(mUser.get(0).mAverageToughness));
        });

        // Toolbar
        Toolbar myToolbar = binding.myToolbar10;
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Buttom Navigation
        BottomNavigationView bottomNav = binding.bottomNav10;
        bottomNav.setSelectedItemId(R.id.person);
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
                case R.id.person:
                    intent = new Intent(context, PersonActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.userSettings:
                    intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        });
    }
}