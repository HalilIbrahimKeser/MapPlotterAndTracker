package com.halil.mapplotterandtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Repository.Repository;
import com.halil.mapplotterandtracker.databinding.ActivityMainBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener, MapEventsReceiver {

    // Binding and context
    ActivityMainBinding binding;
    public Context context;
    // Repo
    private Repository mRepository;
    // Helper
    Helper helper = new Helper();
    MapHelper mapHelper = new MapHelper();
    // Views
    TextView tvAddress;
    // Sensors
    SensorManager sensorManager;
    Sensor accelerometerSensor;
    Boolean accelerometerSensorChanged = false;
    // Location
    LocationManager locationManager;
    RoadManager roadManager = new OSRMRoadManager(this, MY_USER_AGENT);
    private static final String MY_USER_AGENT = "Halil007";

    // Map
    public MapView mapViewOsm;
    public IMapController mapController;
    public MapEventsOverlay mMapEventsOverlay;
    public Polyline roadOverlay;
    public CompassOverlay mCompassOverlay;
    public ArrayList<GeoPoint> waypoints;
    public Road road;
    public Marker nodeMarker;
    public RoadNode node;
    public GeoPoint clickLocation;
    public GeoPoint currentPoint;
    public GeoPoint startPoint;
    public GeoPoint endPoint;
    public Marker currentMarker;
    public Marker startMarker;
    public Marker endMarker;
    boolean positionsSet = false;
    boolean trackingStartet = false;
    public Trip trip;
    int timer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        //Init views
        tvAddress = binding.tvAddress;

        // Toolbar
        Toolbar myToolbar = binding.myToolbar;
        setSupportActionBar(myToolbar);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Buttom Navigation
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.main);
        bottomNav.setOnItemSelectedListener(item -> {
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


        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Repo
        mRepository = new Repository(getApplication());

        // Location

        // Get info from the intent extra
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            // If there is intent extra, than go to method to show the trip from the intent of SavedRoutesActivity
            setTheTripFromIntentExtra();
        }

        // Init Map
        initMap();

        // Testing / Deleting
        // mRepository.deleteTrip(3); mRepository.deleteTrip(4); mRepository.deleteTrip(5);
        // mRepository.deleteTrip(6);mRepository.deleteTrip(7); mRepository.deleteTrip(8); mRepository.deleteTrip(9);
    }

    private void initMap() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mapViewOsm = binding.map;
        mapViewOsm.setTileSource(TileSourceFactory.MAPNIK);
        mapViewOsm.setMultiTouchControls(true);

        // Map Controller
        mapController = mapViewOsm.getController();
        mapController.setZoom(17.0);

        // Add Event overlay and Compass overlay;
        mMapEventsOverlay = new MapEventsOverlay(this);
        mapViewOsm.getOverlays().add(mMapEventsOverlay);

        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapViewOsm);
        mCompassOverlay.enableCompass();
        mapViewOsm.getOverlays().add(mCompassOverlay);

        // Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Last location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set-up start and end points
        waypoints = new ArrayList<>();

        // Gangvei
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        // Current point
        if(location != null) {
            currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        }
        mapController.setCenter(currentPoint);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng currentPosition = new LatLng(latitude, longitude);

        // Initialize start marker and end marker
        startMarker = new Marker(mapViewOsm);
        endMarker = new Marker(mapViewOsm);
        currentMarker = new Marker(mapViewOsm);

        Helper.Deg2UTM curUTM = new Helper.Deg2UTM(currentPosition.latitude, currentPosition.longitude);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint point) {
        clickLocation = new GeoPoint(point.getLatitude(), point.getLongitude());

        if (!trackingStartet && !positionsSet) {
            if (waypoints.size() == 0) {
                startPoint = clickLocation;
                waypoints.add(startPoint);

                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.starticon, null));
                startMarker.setTitle("Start point");
                mapViewOsm.getOverlays().add(startMarker);
                mapViewOsm.invalidate();

            } else if (waypoints.size() == 1) {
                endPoint = clickLocation;
                waypoints.add(endPoint);

                endMarker.setPosition(endPoint);
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                endMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.endicon, null));
                endMarker.setTitle("End point");
                mapViewOsm.getOverlays().add(endMarker);
                // Now we have to points and can draw the road beetween and update the map
                positionsSet = true;
                mapHelper.drawPlannedTrackingline(context, waypoints, positionsSet, trackingStartet, road, roadManager, roadOverlay, mapViewOsm, node, nodeMarker);

            } else {
                Toast.makeText(this, "Remove the waypoints", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Push start to track", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        stopProgram();
        return false;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String locinfo = helper.getLocationInformation(context, location.getLatitude(), location.getLongitude());
        tvAddress.setText(locinfo);

        currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (accelerometerSensorChanged && trackingStartet && positionsSet) {
            mapHelper.setPositionToCurrentLocation(context, currentPoint, currentMarker, mapController, mapViewOsm);

            // Refresh the map!
            mapViewOsm.invalidate();
        }
        // Reset boolean
        accelerometerSensorChanged = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        timer++;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && timer == 60) {
            accelerometerSensor = sensorEvent.sensor;
            float val[] = sensorEvent.values;
            double cal = val[0] * val[0] + val[1] * val[1] + val[2] * val[2];

            Log.d("LOCATIONTEST", "Accel changed " + Math.sqrt(cal));

            accelerometerSensorChanged = true;
            timer = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                mapHelper.setPositionToCurrentLocation(context, currentPoint, currentMarker, mapController, mapViewOsm);
                return true;
            case R.id.menu_start:
                startProgram();
                return true;
            case R.id.menu_stop:
                stopProgram();
                return true;
            case R.id.menu_save:
                mapHelper.saveTrip(context, mRepository, waypoints, road, roadManager);
                return true;
            case R.id.menu_show_saved_routes:
                Intent intent = new Intent(this, PlanedRoutesActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_quit:
                this.doQuit(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    private void setTheTripFromIntentExtra() {
        // Intent extras
        String stringTripID;
        String stringmStartPointLat;
        String stringmStartPointLong;
        String stringmEndPointLat;
        String stringmEndPointLong;

        // values to get from the intent extra strings
        double startPointLat;
        double startPointLong;
        double endPointLat;
        double endPointLong;

        stringTripID = getIntent().getStringExtra("TripID");
        stringmStartPointLat = getIntent().getStringExtra("mStartPointLat");
        stringmStartPointLong = getIntent().getStringExtra("mStartPointLong");
        stringmEndPointLat = getIntent().getStringExtra("mEndPointLat");
        stringmEndPointLong = getIntent().getStringExtra("mEndPointLong");

        if(stringTripID == null) {
            return;
        }

        startPointLat = Double.parseDouble(stringmStartPointLat);
        startPointLong = Double.parseDouble(stringmStartPointLong);
        endPointLat = Double.parseDouble(stringmEndPointLat);
        endPointLong = Double.parseDouble(stringmEndPointLong);

        if(startPointLat != 0) {
            GeoPoint start = new GeoPoint(startPointLat, startPointLong);
            GeoPoint end = new GeoPoint(endPointLat, endPointLong);
            initMap();

            startPoint = start;
            waypoints.add(startPoint);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.starticon, null));
            startMarker.setTitle("Start point");
            mapViewOsm.getOverlays().add(startMarker);

            endPoint = end;
            waypoints.add(endPoint);
            endMarker.setPosition(endPoint);
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            endMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.endicon, null));
            endMarker.setTitle("End point");
            mapViewOsm.getOverlays().add(endMarker);

            trackingStartet = true;
            mapHelper.drawPlannedTrackingline(context, waypoints, positionsSet, trackingStartet, road, roadManager, roadOverlay, mapViewOsm, node, nodeMarker);
        }
    }

    private void startProgram() {
        trackingStartet = true;
    }

    private void stopProgram() {
        if (waypoints.size() == 2) {
            waypoints.remove(startPoint);
            waypoints.remove(endPoint);

            // Remove all overlays
            mapViewOsm.getOverlays().clear();

            // Init map again
            initMap();

            trackingStartet = false;
        }
    }

    private void doQuit(MenuItem item) {
        this.finish();
    }

}