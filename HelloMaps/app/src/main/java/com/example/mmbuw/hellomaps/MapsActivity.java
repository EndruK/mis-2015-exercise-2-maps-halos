package com.example.mmbuw.hellomaps;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapLongClickListener, OnMarkerClickListener, GoogleMap.OnCameraChangeListener {
    private EditText eText;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Set<MarkerOptions> markers;
    private Set<Circle> circles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        eText = (EditText) findViewById(R.id.edittext);
        markers = new HashSet<>();
        circles = new HashSet<>();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onMapLongClick(LatLng point) {
        System.out.println(point);
        System.out.println(eText.getText().toString());
        MarkerOptions m = new MarkerOptions().position(point).title("Marker").snippet(eText.getText().toString());
        markers.add(m);
        mMap.addMarker(m);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0);
        Set<String> s = prefs.getStringSet("save",new HashSet<String>());
        s.add(eText.getText().toString() + ";" + point.latitude + ";" + point.longitude);
        System.out.println(s.size());
        SharedPreferences.Editor e = prefs.edit();
        e.clear();
        e.putStringSet("save",s);
        e.commit();
        System.out.println("added point to sharedPrefs: " + eText.getText().toString() + "  lat:" + point.latitude + "  lat:" + point.longitude);
    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //handle when a user is moving the map
        clearCircles();
        for(MarkerOptions m : markers) {
            LatLngBounds bounding = mMap.getProjection().getVisibleRegion().latLngBounds;
            if(!bounding.contains(m.getPosition())) {

                LatLng ne = bounding.northeast;
                double neLat = ne.latitude;
                double neLon = ne.longitude;
                LatLng sw = bounding.southwest;
                double swLat = sw.latitude;
                double swLon = sw.longitude;

                double distLat = Math.abs(neLat - swLat) * 0.05;
                double distLon = Math.abs(neLon - swLon) * 0.05;

                LatLng newNE = new LatLng(neLat - distLat, neLon - distLon);
                LatLng newSW = new LatLng(swLat + distLat, swLon + distLon);
                LatLng newNW = new LatLng(newNE.latitude,newSW.longitude);
                LatLng newSE = new LatLng(newSW.latitude,newNE.longitude);

                LatLngBounds newBound = new LatLngBounds(newSW,newNE);

                boolean[] pos = new boolean[4];
                Arrays.fill(pos,Boolean.FALSE);
                /*
                1001 | 1000 | 1010
                ------------------
                0001 | 0000 | 0010
                ------------------
                0101 | 0100 | 0110
                */
                if(m.getPosition().latitude > newBound.northeast.latitude) pos[0] = true;
                if(m.getPosition().latitude < newBound.southwest.latitude) pos[1] = true;
                if(m.getPosition().longitude  > newBound.northeast.longitude)  pos[2] = true;
                if(m.getPosition().longitude  < newBound.southwest.longitude)  pos[3] = true;
                double radius = 0d;
                float[] res = new float[1];
                switch (Arrays.toString(pos)) {
                    case "[true, false, false, true]": //links oben
                        radius = dist(m.getPosition(),newNW);
                        break;
                    case "[true, false, false, false]"://oben
                        //radius = Math.abs(m.getPosition().latitude - newNE.latitude);
                        res = new float[1];
                        Location.distanceBetween(m.getPosition().latitude,m.getPosition().longitude,newNE.latitude,m.getPosition().longitude,res);
                        radius = res[0];
                        break;
                    case "[true, false, true, false]"://rechts oben
                        radius = dist(m.getPosition(),newNE);
                        break;
                    case "[false, false, false, true]"://links
                        res = new float[1];
                        Location.distanceBetween(m.getPosition().latitude,m.getPosition().longitude,m.getPosition().latitude,newSW.longitude,res);
                        radius = res[0];
                        break;
                    case "[false, false, false, false]"://mitte
                        break;
                    case "[false, false, true, false]"://rechts
                        res = new float[1];
                        Location.distanceBetween(m.getPosition().latitude,m.getPosition().longitude,m.getPosition().latitude,newNE.longitude,res);
                        radius = res[0];
                        break;
                    case "[false, true, false, true]"://links unten
                        radius = dist(m.getPosition(),newSW);
                        break;
                    case "[false, true, false, false]"://unten
                        res = new float[1];
                        Location.distanceBetween(m.getPosition().latitude,m.getPosition().longitude,newSE.latitude,m.getPosition().longitude,res);
                        radius = res[0];
                        break;
                    case "[false, true, true, false]":// rechts unten
                        radius = dist(m.getPosition(),newSE);
                        break;
                    default:
                        break;
                }
                System.out.println(radius);

                LatLng point = bounding.getCenter();
                CircleOptions c = new CircleOptions().center(m.getPosition()).radius(radius).strokeColor(Color.BLUE);
                Circle circle = mMap.addCircle(c);
                circles.add(circle);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0).edit().clear().commit();
        mMap.setOnMapLongClickListener(this); //add LongKlickListener
        mMap.setOnCameraChangeListener(this); //add CameraChangeListener
        initMarkers();
        mMap.setMyLocationEnabled(true); //activate local position
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //get the location service
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider); //get last known location
        if(location!=null){ //if lastLocation is known
            onLocationChanged(location); //move the cam and update textview
        }
        locationManager.requestLocationUpdates(provider, 20000, 0, this); //update the location if moving
    }

    public void clearCircles() {
        for(Circle c : circles) {
            c.remove();
        }
    }

    public double dist(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(a.latitude,a.longitude,b.latitude,b.longitude,result);
        return (double)result[0];
    }

    public void initMarkers() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0);
        Set<String> s = prefs.getStringSet("save",new HashSet<String>());
        System.out.println(s.size());
        for(String item : s) {
            System.out.println(item);
            String[] tmp = item.split(";");
            System.out.println(tmp[0]);
            String title = "Marker";
            String snippet = tmp[0];
            double lat = Double.parseDouble(tmp[1]);
            double lon = Double.parseDouble(tmp[2]);
            LatLng p = new LatLng(lat,lon);
            markers.add(new MarkerOptions().position(p).title(title).snippet(snippet));
            System.out.println("loaded marker " + snippet + "  lat:" + lat + "  lat:" + lon);
        }
        for(MarkerOptions m : markers) {
            mMap.addMarker(m);
        }
    }

    public void clearMarkers(View view) {
        getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0).edit().clear().commit();
        markers.clear();
        mMap.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView tvLocation = (TextView) findViewById(R.id.tv_location);
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng latLng = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        tvLocation.setText("lat:" +  lat  + ", long:"+ lon );
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}
