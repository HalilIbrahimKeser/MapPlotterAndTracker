package com.halil.mapplotterandtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.databinding.ActivitySavedRoutesBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SavedRoutesActivity extends AppCompatActivity {

    ActivitySavedRoutesBinding binding;
    Intent intent;
    // File
    private static final String FILE_NAME = "trips.txt";
    File file;
    FileReader fileReader;
    Context context;
    RecyclerView recyclerView;

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

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        intent = new Intent(this, MainActivity.class);
        switch (item.getItemId()) {
            case R.id.menu_goback:
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                return true;
            case R.id.menu_goback2:
                intent = new Intent(this, MainActivity.class);
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