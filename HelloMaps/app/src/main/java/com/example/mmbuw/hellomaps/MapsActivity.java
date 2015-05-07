package com.example.mmbuw.hellomaps;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapLongClickListener, OnMarkerClickListener {
    private EditText eText;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        eText = (EditText) findViewById(R.id.edittext);
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
        mMap.addMarker(new MarkerOptions().position(point).title("Marker").snippet(eText.getText().toString()));
        //TODO: put this marker in sharedPreferences
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
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setOnMapLongClickListener(this); //add LongKlickListener
        mMap.setMyLocationEnabled(true); //activate local position
        //TODO: load all Marker from Prefs
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0);
        //getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0).edit().clear().commit();
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
            mMap.addMarker(new MarkerOptions().position(p).title(title).snippet(snippet));
            System.out.println("loaded marker " + snippet + "  lat:" + lat + "  lat:" + lon);
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //get the location service
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider); //get last known location
        if(location!=null){ //if lastLocation is known
            onLocationChanged(location); //move the cam and update textview
        }
        locationManager.requestLocationUpdates(provider, 20000, 0, this); //update the location if moving
    }
    public void clearMarkers(View view) {
        getApplicationContext().getSharedPreferences("com.example.mmbuw.hellomaps.save", 0).edit().clear().commit();
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
