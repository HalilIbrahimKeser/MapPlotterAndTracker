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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    LocationManager lm;
    SensorManager sm;
    Sensor accel;

    MapView osm;


    int timer = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        osm = findViewById(R.id.map);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = osm.getController();
        mapController.setZoom(17.0);


        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        GeoPoint startPoint = new GeoPoint(l.getLatitude(), l.getLongitude());
        mapController.setCenter(startPoint);

        double latitude = l.getLatitude();
        double longitude = l.getLongitude();
        LatLng currentPosition = new LatLng(latitude,longitude);

        Helper.Deg2UTM curUTM = new Helper.Deg2UTM(currentPosition.latitude,currentPosition.longitude);
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

    @Override
    public void onLocationChanged(@NonNull Location location) {

        String locinfo = getLocationInformation(location.getLatitude(),location.getLongitude());
        Toast.makeText(this, locinfo, Toast.LENGTH_SHORT).show();

        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        Marker startMarker = new Marker(osm);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        osm.getOverlays().add(startMarker);

        osm.invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        timer++;

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && timer == 60) {
            accel = sensorEvent.sensor;
            float val[] = sensorEvent.values;
            double cal = val[0]*val[0] + val[1]*val[1] + val[2]*val[2];

            Log.d("LOCATIONTEST", "Accel changed " + Math.sqrt(cal));

            timer = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
}