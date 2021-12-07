package com.halil.mapplotterandtracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.halil.mapplotterandtracker.Entities.Locations;
import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Entities.UserInfo;
import com.halil.mapplotterandtracker.Repository.Repository;
import com.halil.mapplotterandtracker.VievModel.ViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;

import java.util.ArrayList;
import java.util.List;

public class MapWorks {
    public MapView mapViewOsm;
    public Polyline roadOverlay;
    public ArrayList<GeoPoint> waypoints;
    public Road road;
    public Marker nodeMarker;
    public RoadNode node;
    boolean positionsSet = false;
    boolean trackingStartet = false;
    public Trip trip;
    public Trip tripIntent;
    public Context context;
    public RoadManager roadManager;
    public IMapController mapController;
    Location location;
    List<Locations> locationsList;
    public boolean tripIsFinnished;
    // Helper
    LocationHelper locationHelper = new LocationHelper();

    private Repository mRepository;
    ViewModel mViewModel;
    public UserInfo mUser;

    private Polyline mPolyline;
    private ArrayList<GeoPoint> pathPoints = new ArrayList<>();

    public void setPositionToCurrentLocation(Context context, GeoPoint currentPoint, Marker currentMarker, IMapController mapController, MapView mapViewOsm) {
        if (currentPoint != null) {
            currentMarker.setPosition(currentPoint);
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            currentMarker.setIcon(ResourcesCompat.getDrawable(context.getResources(), R.drawable.currentposicon, null));
            currentMarker.setTitle("Your position");
            mapController.setCenter(currentPoint);
            mapViewOsm.getOverlays().add(currentMarker);
        }
    }

    public void drawHikeTrackingline(Context context1, ViewModel viewModel1, Repository repository1, Location location1, ArrayList<GeoPoint> waypoints1, boolean positionsSet1, boolean trackingStartet1,
                                     RoadManager roadManager1, MapView mapViewOsm1, Marker nodeMarker1) {
        context = context1;
        waypoints = waypoints1;
        positionsSet = positionsSet1;
        trackingStartet = trackingStartet1;
        roadManager = roadManager1;
        mapViewOsm = mapViewOsm1;
        nodeMarker = nodeMarker1;
        location = location1;
        mRepository = repository1;
        mViewModel = viewModel1;

        GeoPoint currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        mapViewOsm1.setZoomRounding(true);
        // Map Controller
        mapController = mapViewOsm.getController();
        mapController.setZoom(19.0);
        mapController.setCenter(currentPoint);
        mapController.animateTo(currentPoint);

        // Locations locations = location.
        mPolyline = new Polyline(mapViewOsm);
        pathPoints.add(currentPoint);
        mPolyline.setPoints(pathPoints);

        LatLng currentPosition1 = new LatLng(location.getLatitude(), location.getLongitude());
        LocationHelper.Deg2UTM curUTM = new LocationHelper.Deg2UTM(currentPosition1.latitude, currentPosition1.longitude);

        // Save locations i database
        Locations locations = new Locations(1, location.getLatitude(), location.getLongitude(), location.getAltitude(),
                curUTM.Easting, curUTM.Northing, curUTM.Letter, curUTM.Zone, location.getBearing(), location.getBearingAccuracyDegrees());
        mViewModel.insertLocation(locations);

        final Paint paintBorder = new Paint();
        paintBorder.setStrokeWidth(5);
        paintBorder.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBorder.setColor(Color.DKGRAY);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setAntiAlias(true);

        final Paint paintInside = new Paint();
        paintInside.setStrokeWidth(4);
        paintInside.setStyle(Paint.Style.FILL);
        paintInside.setColor(Color.GREEN);
        paintInside.setStrokeCap(Paint.Cap.ROUND);
        paintInside.setAntiAlias(true);

        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintBorder));
        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintInside));

        mapViewOsm.getOverlays().add(mPolyline);
    }

    public void drawPlannedTrackingline(Context context1, ArrayList<GeoPoint> waypoints1, boolean positionsSet1, boolean trackingStartet1,
                                        RoadManager roadManager1, MapView mapViewOsm1, Marker nodeMarker1) {
        // start kode fra https://github.com/MKergall/osmbonuspack
        context = context1;
        waypoints = waypoints1;
        positionsSet = positionsSet1;
        trackingStartet = trackingStartet1;
        roadManager = roadManager1;
        mapViewOsm = mapViewOsm1;
        nodeMarker = nodeMarker1;

        if (waypoints.size() >= 2 && positionsSet) {
            Thread thread = new Thread(() -> {
                try {
                    // Road between points
                    road = roadManager.getRoad(waypoints);

                    // Build a Polyline with the route shape
                    roadOverlay = RoadManager.buildRoadOverlay(road);
                    roadOverlay.setGeodesic(true);
                    roadOverlay.showInfoWindow();

                    // Add this Polyline to the overlays to the map
                    if (road.mNodes.size() > 0) {
                        mapViewOsm.getOverlays().add(roadOverlay);
                    } else {
                        Looper.prepare();
                        Toast.makeText(context, "No nodes to draw, fail in roadManager. Have you allowed internet permissions?", Toast.LENGTH_SHORT).show();
                    }

                    // Adds nodes to the rode
                    Drawable nodeIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.markernode, null);
                    for (int i = 0; i < road.mNodes.size(); i++) {
                        node = road.mNodes.get(i);
                        nodeMarker = new Marker(mapViewOsm);
                        nodeMarker.setPosition(node.mLocation);
                        nodeMarker.setIcon(nodeIcon);
                        nodeMarker.setTitle("Step " + i);
                        mapViewOsm.getOverlays().add(nodeMarker);

                        nodeMarker.setSnippet(node.mInstructions);
                        nodeMarker.setSubDescription(Road.getLengthDurationText(context, node.mLength, node.mDuration));
                        Drawable icon = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.continueicon, null);
                        nodeMarker.setImage(icon);
                    }

                    // Update map
                    mapViewOsm.invalidate();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        } else {
            Toast.makeText(context.getApplicationContext(), "Push start to track", Toast.LENGTH_SHORT).show();
        }
    }

    // SAVE, must be in this helper file, bacause of the "roadOverlay" in the method drawPlannedTrackingline()
    public void saveTrip(boolean isFinished1, Trip tripIntent1, Trip trip1, ViewModel viewModel1, Context context1, Repository mRepository1, ArrayList<GeoPoint> waypoints1, Road road1,
                         RoadManager roadManager1, List<Locations> locationsList1) {
        // Create a trip and save it to file
        context = context1;
        mRepository = mRepository1;
        waypoints = waypoints1;
        road = road1;
        roadManager = roadManager1;
        tripIsFinnished = isFinished1;
        mViewModel = viewModel1;
        locationsList = locationsList1;
        tripIntent = tripIntent1;
        trip = trip1;

        ArrayList<Locations> locationsListTemp = new ArrayList<Locations>();
        locationsListTemp.addAll(locationsList);

        Thread thread1 = null;

        thread1 = new Thread(() -> {
            if (tripIntent == null) {
                if (waypoints.size() != 0) {
                    try {
                        Looper.prepare();

                        List<UserInfo> mUserList = mViewModel.getAllUser();
                        mUser = mUserList.get(0);

                        // Road between points
                        road = roadManager.getRoad(waypoints);

                        String mFromAdress = locationHelper.getLocationInformation(context, waypoints.get(0).getLatitude(), waypoints.get(0).getLongitude());
                        String mToAdress = locationHelper.getLocationInformation(context, waypoints.get(1).getLatitude(), waypoints.get(1).getLongitude());
                        double mLength = road.mLength;
                        double mNodes = road.mNodes.size();
                        double mDuration = road.mDuration;
                        double mDistance = roadOverlay.getDistance();

                        double mStartAltitude = waypoints.get(0).getAltitude();
                        double mEndAltitude = waypoints.get(1).getAltitude();
                        double mElevation = Math.max(mStartAltitude, mEndAltitude);

                        double mStartPointLat = waypoints.get(0).getLatitude();
                        double mStartPointLong = waypoints.get(0).getLongitude();
                        double mEndPointLat = waypoints.get(1).getLatitude();
                        double mEndPointLong = waypoints.get(1).getLongitude();

                        Trip.StartGeo startGeo = new Trip.StartGeo(mStartPointLat, mStartPointLong, mStartAltitude);
                        Trip.StopGeo stopGeo = new Trip.StopGeo(mEndPointLat, mEndPointLong, mEndAltitude);
                        double mEstimatedToughness = calculateToughness(mDistance, mStartAltitude, mEndAltitude);

                        if (mUser != null) {
                            // Save trip
                            trip = new Trip(mUser.mUserinfoID, mFromAdress, mToAdress, mLength, mNodes, mDuration, mDistance, mElevation, startGeo, stopGeo,
                                    tripIsFinnished, mEstimatedToughness);
                            // Insert the trip to db
                            mViewModel.insertTrip(trip);

                            // Set location id's same as trip id
                            if (locationsListTemp.size() > 0) {
                                // Get all trips fom db
                                List<Trip> mTripList = mViewModel.getAllTripsAll();
                                if (mTripList != null) {

                                    // Get the trip from above that was inserted to db
                                    final List<Trip> tripToUpdate = new ArrayList<Trip>();
                                    for (Trip trip2 : mTripList) {
                                        if (trip2.mFromAddress.contains(trip.mFromAddress) && trip2.mDistance == trip.mDistance) {
                                            tripToUpdate.add(trip2);
                                        }
                                    }

                                    // Found the trip
                                    Trip trip3 = tripToUpdate.get(0);
                                    for (int i = 0; i < locationsListTemp.size(); i++) {
                                        locationsListTemp.get(i).setmLocationTripId(trip3.mTripId);
                                    }
                                    mRepository.insertLocations(locationsListTemp);
                                }
                            }

                            Toast.makeText(context.getApplicationContext(), "Trip saved", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "No user info, fail in db", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "No trip to save. Create a trip first", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Trip already saved", Toast.LENGTH_SHORT).show();
            }
        });
        thread1.start();


    }

    private double calculateToughness(double mDistance, double mStartAltitude, double mEndAltitude) {
        double altitudePoints = 0;
        double distancePoints = 0;

        if (mStartAltitude > mEndAltitude) {
            // Går nedover bakke, 2 poeng for hver meter differanse
            altitudePoints = (mStartAltitude - mEndAltitude) * 1;
        } else if (mStartAltitude < mEndAltitude) {
            // Går oppover bakke, 2 poeng for hver meter differanse
            altitudePoints = (mEndAltitude - mStartAltitude) * 3;
        }

        // Distanse i meter
        distancePoints = mDistance * 1;

        return altitudePoints + distancePoints;
    }

    public void updateTrip(boolean mIsFinished, Trip tripIntent1, Trip trip2, ViewModel mViewModel1, Context context1, Repository mRepository1, ArrayList<GeoPoint> waypoints1,
                           Road road1, RoadManager roadManager1, List<Locations> locationsList1) {
        mViewModel = mViewModel1;
        tripIsFinnished = mIsFinished;
        tripIntent = tripIntent1;
        trip = trip2;
        waypoints = waypoints1;
        context = context1;
        mRepository = mRepository1;
        road = road1;
        roadManager = roadManager1;
        locationsList = locationsList1;

        final List<Trip> tripToUpdate = null;

        if (tripIntent != null) {
            Thread thread1 = null;
            thread1 = new Thread(() -> {
                try {
                    //Looper.prepare();
                    List<Trip> mTripList = mViewModel.getAllTripsAll();

                    tripIntent.mIsFinished = mIsFinished;
                    mViewModel.updateTrip(tripIntent);

                    if (mTripList != null) {
                        for (Trip trip : mTripList) {
                            if (trip.mFromAddress.contains(tripIntent.mFromAddress)) {
                                tripToUpdate.add(trip);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
        } else {
            saveTrip(tripIsFinnished, null, trip, mViewModel, context, mRepository, waypoints, road, roadManager, locationsList);
        }
    }
}
