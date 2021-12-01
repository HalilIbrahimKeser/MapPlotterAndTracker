package com.halil.mapplotterandtracker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.content.res.ResourcesCompat;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.Repository.Repository;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import java.util.ArrayList;
import java.util.List;


public class MapHelper {
    public MapView mapViewOsm;
    public Polyline roadOverlay;
    public ArrayList<GeoPoint> waypoints;
    public Road road;
    public Marker nodeMarker;
    public RoadNode node;
    boolean positionsSet = false;
    boolean trackingStartet = false;
    public Trip trip;
    public Context context;
    public RoadManager roadManager;
    // Helper
    Helper helper = new Helper();

    private Repository mRepository;


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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Road between points
                        road = roadManager.getRoad(waypoints);

                        // Build a Polyline with the route shape
                        roadOverlay = RoadManager.buildRoadOverlay(road);
                        roadOverlay.setGeodesic(true);
                        roadOverlay.showInfoWindow();
                        InfoWindow info = roadOverlay.getInfoWindow();

                        List<MilestoneManager> milestones = new ArrayList<MilestoneManager>();
                        roadOverlay.setMilestoneManagers(milestones);

                        // Add this Polyline to the overlays to the map
                        if (road.mNodes.size() > 0) {
                            mapViewOsm.getOverlays().add(roadOverlay);
                        } else {
                            Toast.makeText(context, "No nodes to draw, fail in roadManager", Toast.LENGTH_SHORT).show();
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
                }
            });
            thread.start();
        } else {
            Toast.makeText(context.getApplicationContext(), "Push start to track", Toast.LENGTH_SHORT).show();
        }
    }

    // SAVE, must be in this helper file, bacause og the "roadOverlay" in method drawPlannedTrackingline()
    public void saveTrip(Context context1, Repository mRepository1, ArrayList<GeoPoint> waypoints1, Road road1,
                         RoadManager roadManager1) {
        // Create a trip and save it to file
        context = context1;
        mRepository = mRepository1;
        waypoints = waypoints1;
        road = road1;
        roadManager = roadManager1;

        if (waypoints.size() != 0) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Road between points
                        road = roadManager.getRoad(waypoints);

                        String mFromAdress = helper.getLocationInformation(context, waypoints.get(0).getLatitude(), waypoints.get(0).getLongitude());
                        String mToAdress = helper.getLocationInformation(context, waypoints.get(1).getLatitude(), waypoints.get(1).getLongitude());
                        double mLength = road.mLength;
                        double mNodes = road.mNodes.size();
                        double mDuration = road.mDuration;
                        double mDistance = roadOverlay.getDistance();
                        double mElevation = Math.max(waypoints.get(0).getAltitude(), waypoints.get(1).getAltitude());
                        double mStartPointLat = waypoints.get(0).getLatitude();
                        double mStartPointLong = waypoints.get(0).getLongitude();
                        double mEndPointLat = waypoints.get(1).getLatitude();
                        double mEndPointLong = waypoints.get(1).getLongitude();

                        Trip.StartGeo startGeo = new Trip.StartGeo(mStartPointLat, mStartPointLong);
                        Trip.StopGeo stopGeo = new Trip.StopGeo(mEndPointLat, mEndPointLong);
                        Trip trip = new Trip(mFromAdress, mToAdress, mLength, mNodes, mDuration, mDistance, mElevation, startGeo, stopGeo, false);

                        // Save trip
                        mRepository.tripInsert(trip);

                        Looper.prepare();

                        Toast.makeText(context.getApplicationContext(), "Trip saved", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }else {
            Toast.makeText(context, "No trip to save. Create a trip first", Toast.LENGTH_SHORT).show();
        }
    }
}
