package com.halil.mapplotterandtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Repository.Repository;
import com.halil.mapplotterandtracker.VievModel.ViewModel;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener, MapEventsReceiver {

    // Binding and context
    ActivityMainBinding binding;
    public Context context;
    // ViewModel
    private ViewModel mViewModel;
    Trip mCurentHiking;
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
    Location location;
    LocationManager locationManager;
    RoadManager roadManager = new OSRMRoadManager(this, MY_USER_AGENT);
    private static final String MY_USER_AGENT = "Halil007";
    List<Locations> locationsList = null;

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

    // Permissions
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 2;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Text views
        tvAddress = binding.tvAddress;

        // Toolbar
        Toolbar myToolbar = binding.myToolbar;
        setSupportActionBar(myToolbar);

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

        // View model
        mViewModel = new ViewModelProvider(this).get(ViewModel.class);

        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Repo
        mRepository = new Repository(getApplication());

        // If there is intent extra, than go to method to show the trip from the intent of SavedRoutesActivity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            setTheTripFromIntentExtra();
        }

        // Init Map
        initMap();

        // Testing / Deleting
        // mRepository.deleteTrip(3); mRepository.deleteTrip(4); mRepository.deleteTrip(5);mRepository.deleteTrip(6);
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
            checkPermissions();
            return;
        }
        // Last location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set-up start and end points
        waypoints = new ArrayList<>();

        // Gangvei
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        // Current point
        if(location != null) {
            currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        }
        mapController.setCenter(currentPoint);
        assert location != null;
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
                mapHelper.drawPlannedTrackingline(context, waypoints, true, false, roadManager, mapViewOsm, nodeMarker);
                positionsSet = true;
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
            mapHelper.drawHikeTrackingline(context, mViewModel, mRepository, location, waypoints, true, true, roadManager, mapViewOsm, nodeMarker);
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
        mCurentHiking = mViewModel.getCurrentTrip().getValue();
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle");
        List<Trip> trip = (ArrayList<Trip>) args.getSerializable("arraylist");

        if(trip.size() == 0) {
            return;
        }else {
            GeoPoint start = new GeoPoint(trip.get(0).startGeo.mStartPointLat, trip.get(0).startGeo.mStartPointLong);
            GeoPoint end = new GeoPoint(trip.get(0).stopGeo.mEndPointLat, trip.get(0).stopGeo.mEndPointLong);

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
            mapHelper.drawPlannedTrackingline(context, waypoints, true, false, roadManager, mapViewOsm, nodeMarker);
            positionsSet = true;
            trackingStartet = false;
        }
    }

    private void startProgram() {
        trackingStartet = true;
        if (positionsSet) {
            mapHelper.drawHikeTrackingline(context, mViewModel, mRepository, location, waypoints, true, true, roadManager, mapViewOsm, nodeMarker);
        } else {
            Toast.makeText(this, "Create a start and end point", Toast.LENGTH_SHORT).show();
        }

    }

    private void stopProgram() {
        if(waypoints != null) {

            mViewModel.getAllLocations().observe(this, locations -> {
                locationsList.addAll(locations);
            });
            //slett alle loc i db
//            mViewModel.de
            if (waypoints.size() == 2) {
                waypoints.remove(startPoint);
                waypoints.remove(endPoint);

                // Remove all overlays
                mapViewOsm.getOverlays().clear();

                // Init map again
                initMap();

                positionsSet = false;
                trackingStartet = false;
            }
        }else {
            Toast.makeText(this, "Gps fail", Toast.LENGTH_SHORT).show();
        }

    }

    private void doQuit(MenuItem item) {
        this.finish();
    }

    protected void checkPermissions() {
        //https://developer.here.com/documentation/android-premium/3.17/dev_guide/topics/request-android-permissions.html
        final List<String> missingPermissions = new ArrayList<String>();

        for (final String permission : REQUIRED_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            final String[] permissions = missingPermissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_PERMISSIONS,
                    grantResults);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Endret, https://developer.here.com/documentation/android-premium/3.17/dev_guide/topics/request-android-permissions.html
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && permissions.length == grantResults.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Required permission '" + permissions[i]
                                + "' not granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Required permission '" + permissions[i]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}