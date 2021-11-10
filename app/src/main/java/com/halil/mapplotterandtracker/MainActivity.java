package com.halil.mapplotterandtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.MenuInflater;
import android.view.MenuItem;
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
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private static final String MY_USER_AGENT = "Halil";
    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Polyline mPolyline;
    ActivityMainBinding binding;
    LocationManager locationManager;
    RoadManager roadManager = new OSRMRoadManager(this, MY_USER_AGENT);
    SensorManager sensorManager;
    Sensor accelerometerSensor;

    MapView mapViewOsm;
    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private MinimapOverlay mMinimapOverlay;
    ArrayList<GeoPoint> waypoints;
    Road road;

    int timer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

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

        mapViewOsm = findViewById(R.id.map);
        mapViewOsm.setTileSource(TileSourceFactory.MAPNIK);
        mapViewOsm.setMultiTouchControls(true);

        // Map Controller
        IMapController mapController = mapViewOsm.getController();
        mapController.setZoom(17.0);

        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapViewOsm);
        this.mCompassOverlay.enableCompass();
        mapViewOsm.getOverlays().add(this.mCompassOverlay);

        // Permissions
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 123);
        }

        // Last location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(startPoint);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng currentPosition = new LatLng(latitude,longitude);

        // Gangvei
        ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        // Set-up start and end points
        waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        GeoPoint endPoint = new GeoPoint(59.950030, 11.014363); //midlertidig
        waypoints.add(endPoint);


        Helper.Deg2UTM curUTM = new Helper.Deg2UTM(currentPosition.latitude,currentPosition.longitude);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        String locinfo = getLocationInformation(location.getLatitude(),location.getLongitude());
        //Toast.makeText(this, locinfo, Toast.LENGTH_SHORT).show();

        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        Marker startMarker = new Marker(mapViewOsm);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapViewOsm.getOverlays().add(startMarker);

        startMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24, null));
        startMarker.setTitle("Start point");

        // Draw tracking line
        drawPolyline();

        // Refresh the map!
        mapViewOsm.invalidate();
    }

    private void drawPolyline() {
        Thread thread = new Thread(() -> {
            try  {
                // Road between points
                road = roadManager.getRoad(waypoints);

                // Build a Polyline with the route shape
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

                // Add this Polyline to the overlays to the map
                mapViewOsm.getOverlays().add(roadOverlay);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        timer++;

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && timer == 60) {
            accelerometerSensor = sensorEvent.sensor;
            float val[] = sensorEvent.values;
            double cal = val[0]*val[0] + val[1]*val[1] + val[2]*val[2];

            Log.d("LOCATIONTEST", "Accel changed " + Math.sqrt(cal));

            timer = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                //newGame();
                return true;
            case R.id.menu_start:
                return true;
            case R.id.menu_stop:
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    private void doQuit(MenuItem item) {
        this.finish();
    }
}