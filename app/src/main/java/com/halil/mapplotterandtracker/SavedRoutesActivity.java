package com.halil.mapplotterandtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halil.mapplotterandtracker.Adapter.AdapterClass;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.VievModel.ViewModel;
import com.halil.mapplotterandtracker.databinding.ActivitySavedRoutesBinding;

import java.util.Objects;

public class SavedRoutesActivity extends AppCompatActivity {

    private static Trip trip;
    ActivitySavedRoutesBinding binding;
    Intent intent;
    Context context;
    RecyclerView recyclerView;

    // View model
    ViewModel mViewModel;
    AdapterClass adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivitySavedRoutesBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        context = getApplicationContext();

        // Toolbar
        Toolbar myToolbar = binding.myToolbar2;
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Recyclerview
        recyclerView = binding.recyclerview;

        // ViewModel
        mViewModel = new ViewModelProvider(this).get(ViewModel.class);
        mViewModel.getAllTrips().observe(this, trips -> {
            adapter = new AdapterClass(this, trips);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter.setClickListener(new AdapterClass.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("TripID", String.valueOf(trips.get(position).mTripId));
                    intent.putExtra("mStartPointLat", String.valueOf(trips.get(position).mStartPointLat));
                    intent.putExtra("mStartPointLong", String.valueOf(trips.get(position).mStartPointLong));
                    intent.putExtra("mEndPointLat", String.valueOf(trips.get(position).mEndPointLat));
                    intent.putExtra("mEndPointLong", String.valueOf(trips.get(position).mEndPointLong));
                    startActivity(intent);
                }
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