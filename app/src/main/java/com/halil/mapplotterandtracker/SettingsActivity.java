package com.halil.mapplotterandtracker;

import static io.reactivex.internal.schedulers.SchedulerPoolFactory.start;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.Entities.UserInfo;
import com.halil.mapplotterandtracker.Repository.Repository;
import com.halil.mapplotterandtracker.VievModel.ViewModel;
import com.halil.mapplotterandtracker.databinding.ActivityMainBinding;
import com.halil.mapplotterandtracker.databinding.ActivitySettingsBinding;

import org.osmdroid.config.Configuration;

import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity{

    ActivitySettingsBinding binding;
    public Context context;

    Button btSavePerson;
    Button btUpdatePerson;
    EditText etName;
    EditText etAge;
    EditText etWeight;

    private Repository mRepository;
    private ViewModel mViewModel;
    public List<UserInfo> mUser;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivitySettingsBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Repo
        mRepository = new Repository(getApplication());

        btSavePerson = binding.btSavePerson;
        btUpdatePerson = binding.btUpdatePerson;
        etName = binding.etName;
        etAge = binding.etAge;
        etWeight = binding.etWeight;

        // View model
        mViewModel = new ViewModelProvider(this).get(ViewModel.class);
        mViewModel.getSingleUser(1).observe(this, user -> {
            mUser = user;
            etName.setText(mUser.get(0).mName);
            etAge.setText(String.valueOf(mUser.get(0).mBirthYear));
            etWeight.setText(String.valueOf(mUser.get(0).mWeight));
        });

        btSavePerson.setOnClickListener(v -> {
            String nameString = etName.getText().toString();
            int etAgeInt = Integer.parseInt(etName.getText().toString());
            int etWeightInt = Integer.parseInt(etWeight.getText().toString());

            mRepository.deleteUser(1);
            UserInfo userInfo = new UserInfo(1, nameString, etAgeInt, etWeightInt, 0, 0);
            mRepository.userInfoInsert(userInfo);
        });

        btUpdatePerson.setOnClickListener(v -> {
            String nameString = etName.getText().toString();
            int etAgeInt = Integer.parseInt(etAge.getText().toString());
            int etWeightInt = Integer.parseInt(etWeight.getText().toString());
            mUser.get(0).mName = nameString;
            mUser.get(0).mBirthYear = etAgeInt;
            mUser.get(0).mWeight = etWeightInt;

            mRepository.updateUser(mUser.get(0));
            Toast.makeText(this, mUser.get(0).mWeight + " updated" + "Age", Toast.LENGTH_LONG).show();

        });

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
    }
}