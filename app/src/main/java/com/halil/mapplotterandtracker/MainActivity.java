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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;
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
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener, MapEventsReceiver {

    // Permission
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    // Binding and context
    ActivityMainBinding binding;
    public Context context;
    // ViewModel
    private ViewModel mViewModel;
    Trip mCurrentHiking;
    // Repo
    private Repository mRepository;
    // Helper
    LocationHelper locationHelper = new LocationHelper();
    MapWorks mapWorks = new MapWorks();
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
    List<Locations> locationsList = new ArrayList<Locations>();

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
    boolean activateLoacationUpdates = true;
    public Trip trip;
    List<Trip> tripFromIntent;
    List<Trip> tripFromDb;
    int timer = 0;

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

        requestLocationPermission();

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
        initMap(waypoints);

        // Testing / Deleting / Resetting all locations
        // mRepository.deleteTrip(5); mRepository.deleteTrip(6)
        // mRepository.resetAllRecordedTrips();
        // mRepository.resetAllLocations();
    }

    public void initMap(ArrayList<GeoPoint> waypoints1) {
        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);

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
        // Last location
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        // Bruker Gps
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set-up start and end points
        // If got waypoints from planned trips, then set it
        if(waypoints1 != null) {
            waypoints = waypoints1;
        } else {
            waypoints = new ArrayList<>();
        }

        // Gangvei
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        // Current point
        double latitude;
        double longitude;
        LatLng currentPosition = null;
        LocationHelper.Deg2UTM curUTM;
        if (location != null) {
            currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setCenter(currentPoint);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            currentPosition = new LatLng(latitude, longitude);
            curUTM = new LocationHelper.Deg2UTM(currentPosition.latitude, currentPosition.longitude);
        }

        // Initialize start marker and end marker
        startMarker = new Marker(mapViewOsm);
        endMarker = new Marker(mapViewOsm);
        currentMarker = new Marker(mapViewOsm);
    }

    public void setTheTripFromIntentExtra() {
        //En intent er sendt fra annen aktivitet. Da tegnes denne ruten. Inten er enten fra Recorded eller Planned routes.
        mCurrentHiking = mViewModel.getCurrentTrip().getValue();

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle");
        tripFromIntent = (ArrayList<Trip>) args.getSerializable("arraylist");

        if (tripFromIntent.size() == 0) {
            return;
        } else {
            GeoPoint start = new GeoPoint(tripFromIntent.get(0).startGeo.mStartPointLat, tripFromIntent.get(0).startGeo.mStartPointLong);
            GeoPoint end = new GeoPoint(tripFromIntent.get(0).stopGeo.mEndPointLat, tripFromIntent.get(0).stopGeo.mEndPointLong);

            initMap(waypoints);

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
            mapWorks.drawPlannedTrackingline(context, waypoints, true, false, roadManager, mapViewOsm, nodeMarker);
            positionsSet = true;
            trackingStartet = false;
        }
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
                mapWorks.drawPlannedTrackingline(context, waypoints, true, false, roadManager, mapViewOsm, nodeMarker);
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
        String locinfo = locationHelper.getLocationInformation(context, location.getLatitude(), location.getLongitude());
        tvAddress.setText(locinfo);

        currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (accelerometerSensorChanged && trackingStartet && positionsSet) {
            if (activateLoacationUpdates) {
                mapWorks.setPositionToCurrentLocation(context, currentPoint, currentMarker, mapController, mapViewOsm);
                mapWorks.drawHikeTrackingline(context, mViewModel, mRepository, location, waypoints, true, true, roadManager, mapViewOsm, nodeMarker);

                // Refresh the map!
                mapViewOsm.invalidate();
            }

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
                mapWorks.setPositionToCurrentLocation(context, currentPoint, currentMarker, mapController, mapViewOsm);
                return true;
            case R.id.menu_start:
                startProgram();
                return true;
            case R.id.menu_stop:
                stopProgram();
                return true;
            case R.id.menu_save:
                //Save or update planed route
                if (tripFromIntent != null) {
                    // Trip is from the planned trips
                    if (trackingStartet) {
                        mapWorks.updateTrip(true, tripFromIntent.get(0), null, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
                        Toast.makeText(context, "Yay! finished hiking. Congrats!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Nothing to update, start hiking", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // New trip
                    if (trackingStartet) {
                        Toast.makeText(context, "Yay! finished hiking. Congrats!", Toast.LENGTH_SHORT).show();
                        mapWorks.saveTrip(true, null, trip, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
                    } else {
                        Toast.makeText(context, "Tracking not started, saving trip on planned trips", Toast.LENGTH_SHORT).show();
                        mapWorks.saveTrip(false, null, trip, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
                    }
                }
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

    private void startProgram() {
        if (positionsSet) {
            if (!trackingStartet) {
                // Start knapp er trykket
                trackingStartet = true;
                activateLoacationUpdates = true;

                mapWorks.setPositionToCurrentLocation(context, currentPoint, currentMarker, mapController, mapViewOsm);

                mapWorks.drawHikeTrackingline(context, mViewModel, mRepository, location, waypoints, true, true, roadManager, mapViewOsm, nodeMarker);
            } else {
                // Start knapp er trykket enda en gang
                Toast.makeText(this, "Tracking already started. Start hiking! Good luck!", Toast.LENGTH_LONG).show();
            }
        } else {
            // Start knapp er trykket enda en gang
            Toast.makeText(this, "Start and end point not set. Single tap on Screen. Hold to remove.", Toast.LENGTH_LONG).show();
        }
    }

    private void stopProgram() {
        if (waypoints != null) {
            if (trackingStartet) {
                mViewModel.getAllLocations().observe(this, locations -> {
                    if (locations != null) {
                        // Oppdater location list
                        locationsList.addAll(locations);

                      // Delete those locations in list from db. They will be added later with right trip id
//                        if (locationsList.size() != 0) {
//                            mViewModel.deleteAllLocations(locationsList);
//                        }
                    } else {
                        Toast.makeText(this, "No data got from saved locations.", Toast.LENGTH_LONG).show();
                    }
                });

                // Save the trip as finished
                if (tripFromIntent != null) {
                    Toast.makeText(this, "Updating your planned trip.", Toast.LENGTH_SHORT).show();
                    mapWorks.updateTrip(true, tripFromIntent.get(0), null, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
                } else {
                    Toast.makeText(this, "Saving your new trip.", Toast.LENGTH_SHORT).show();
                    // Stop location updates
                    activateLoacationUpdates = false;
                    mapWorks.saveTrip(true, null, null, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
                }

            } else {
                // Reset map and waypoints
                if (waypoints.size() == 1) {
                    waypoints.remove(startPoint);
                    resetMap();

                } else if (waypoints.size() == 2) {
                    waypoints.remove(startPoint);
                    waypoints.remove(endPoint);
                    resetMap();
                }
                Toast.makeText(this, "Tracking not started. Stopped the program. No track to save",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Gps fail", Toast.LENGTH_SHORT).show();
        }
    }

    public void resetMap() {
        activateLoacationUpdates = true;

        // Remove all overlays
        mapViewOsm.getOverlays().clear();

        // Init map again
        initMap(waypoints);

        positionsSet = false;
        trackingStartet = false;
        tripFromIntent = null;
    }

    private void doQuit(MenuItem item) {
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(MY_PERMISSIONS_REQUEST_LOCATION)
    public void requestLocationPermission() {
        if(EasyPermissions.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the permission", MY_PERMISSIONS_REQUEST_LOCATION, REQUIRED_PERMISSIONS);
        }
    }
}