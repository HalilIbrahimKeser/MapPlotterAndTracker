package com.halil.mapplotterandtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
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
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener, MapEventsReceiver {

    // Permissions
    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    // Binding
    ActivityMainBinding binding;

    // Views
    TextView tvAddress;

    // Sensor
    SensorManager sensorManager;
    Sensor accelerometerSensor;
    Boolean accelerometerSensorChanged = false;

    // Location
    LocationManager locationManager;
    RoadManager roadManager = new OSRMRoadManager(this, MY_USER_AGENT);
    private static final String MY_USER_AGENT = "Halil007";

    // MAP
    MapView mapViewOsm;
    IMapController mapController;
    MapEventsOverlay mMapEventsOverlay;
    Polyline roadOverlay;
    CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private MinimapOverlay mMinimapOverlay;
    private Polyline mPolyline;
    ArrayList<GeoPoint> waypoints;
    Road road;
    Marker nodeMarker;
    RoadNode node;
    GeoPoint clickLocation;
    GeoPoint currentPoint;
    GeoPoint startPoint;
    GeoPoint endPoint;
    Marker currentMarker;
    Marker startMarker;
    Marker endMarker;
    boolean trackingStartet = false;

    int timer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //Init views
        tvAddress = binding.tvAddress;

        // Toolbar
        Toolbar myToolbar = binding.myToolbar;
        setSupportActionBar(myToolbar);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Init Map
        initMap();
    }

    private void initMap() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mapViewOsm = binding.map;
        mapViewOsm.setTileSource(TileSourceFactory.MAPNIK);
        mapViewOsm.setMultiTouchControls(true);

        // Map Controller
        mapController = mapViewOsm.getController();
        mapController.setZoom(17.0);

        // Event overlay
        mMapEventsOverlay = new MapEventsOverlay(this);
        mapViewOsm.getOverlays().add(0, mMapEventsOverlay);

        // Compass overlay;
        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapViewOsm);
        mCompassOverlay.enableCompass();
        mapViewOsm.getOverlays().add(mCompassOverlay);

        // Permissions
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 123);
        }
        // Får feil melding når denne permission ikke er lagt til
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
        ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        // Current point
        currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(currentPoint);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng currentPosition = new LatLng(latitude,longitude);
        //waypoints.add(currentPoint);

        // Initialize start marker and end marker
        startMarker = new Marker(mapViewOsm);
        endMarker = new Marker(mapViewOsm);
        currentMarker = new Marker(mapViewOsm);

        Helper.Deg2UTM curUTM = new Helper.Deg2UTM(currentPosition.latitude,currentPosition.longitude);
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        stopProgram();
        return false;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint point) {
        clickLocation = new GeoPoint(point.getLatitude(), point.getLongitude());

        if (waypoints.size() < 1) {
            //waypoints.remove(startPoint);
            startPoint = clickLocation;
            waypoints.add(startPoint);

            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.starticon, null));
            startMarker.setTitle("Start point");
            mapViewOsm.getOverlays().add(startMarker);
            mapViewOsm.invalidate();

        } else if (waypoints.size() == 1) {
            //waypoints.remove(endPoint);
            endPoint = clickLocation;
            waypoints.add(endPoint);

            endMarker.setPosition(endPoint);
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            endMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.endicon, null));
            endMarker.setTitle("End point");
            mapViewOsm.getOverlays().add(endMarker);

            drawTrackingline();
            mapViewOsm.invalidate();
        }
        return false;
    }

    private void drawTrackingline() {
        Thread thread = new Thread(() -> {
            if(waypoints.size() >= 2) {
                try  {
                    // Road between points
                    road = roadManager.getRoad(waypoints);

                    // Build a Polyline with the route shape
                    roadOverlay = RoadManager.buildRoadOverlay(road);

                    // Add this Polyline to the overlays to the map
                    mapViewOsm.getOverlays().add(roadOverlay);

                    // Adds nodes to the rode
                    Drawable nodeIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.markernode, null);
                    for (int i=0; i<road.mNodes.size(); i++){
                        node = road.mNodes.get(i);
                        nodeMarker = new Marker(mapViewOsm);
                        nodeMarker.setPosition(node.mLocation);
                        nodeMarker.setIcon(nodeIcon);
                        nodeMarker.setTitle("Step "+ i );
                        mapViewOsm.getOverlays().add(nodeMarker);

                        nodeMarker.setSnippet(node.mInstructions);
                        nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
                        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.mipmap.continueicon, null);
                        nodeMarker.setImage(icon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String locinfo = getLocationInformation(location.getLatitude(),location.getLongitude());
        //Toast.makeText(this, locinfo, Toast.LENGTH_SHORT).show();
        tvAddress.setText(locinfo);

        currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if(accelerometerSensorChanged) {
            // Reset position. Set current location to position on the map
            setPositionToCurrentLocation();

            // Refresh the map!
            mapViewOsm.invalidate();
        }
        // Reset boolean
        accelerometerSensorChanged = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        timer++;

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && timer == 60) {
            accelerometerSensor = sensorEvent.sensor;
            float val[] = sensorEvent.values;
            double cal = val[0]*val[0] + val[1]*val[1] + val[2]*val[2];

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
                setPositionToCurrentLocation();
                return true;
            case R.id.menu_start:
                startProgram();
                return true;
            case R.id.menu_stop:
                stopProgram();
                return true;
            case R.id.menu_save:

                return true;
            case R.id.menu_show_saved_routes:

                return true;
            case R.id.menu_quit:
                this.doQuit(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setPositionToCurrentLocation() {
        if (currentPoint != null) {
            currentMarker.setPosition(currentPoint);
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            currentMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.currentposicon, null));
            currentMarker.setTitle("Your position");
            mapController.setCenter(currentPoint);

            mapViewOsm.getOverlays().add(currentMarker);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    private static boolean hasPermissions(Context context, String... perm){
        if(context!=null && perm!=null){
            for(String p: perm){
                if(ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    private String getLocationInformation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText( this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
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
            // Add event (click) listener and compas back
            mapViewOsm.getOverlays().add(mMapEventsOverlay);
            mapViewOsm.getOverlays().add(mCompassOverlay);

            mapViewOsm.invalidate();

            trackingStartet = false;
        }
    }

    // Exit application
    private void doQuit(MenuItem item) {
        this.finish();
    }
}