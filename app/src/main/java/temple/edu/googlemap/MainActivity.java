package temple.edu.googlemap;

import android.Manifest;
import android.app.Dialog;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    public static final String GEOFENCE_ID = "MyGeofenceId";
    public LatLng lastPinnedLL = null;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_maps);
            Toast.makeText(this, "Perfect!!!", Toast.LENGTH_LONG).show();
            initMap();
        } else {
            // no google maps layout
        }
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }


    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(300);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Button curButton = (Button) findViewById(R.id.current_location);
        curButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location curLocation = getCurrentLocation();
                LatLng current = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(current).title("I am here!"));
                lastPinnedLL = current;
            }
        });

        Button clearButton = (Button) findViewById(R.id.clear_pin);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGoogleMap.clear();
                //lastPinnedLL = null;
            }
        });
    }

    private Location getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Location curLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        return curLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        final Location loc = location;
        final ToggleButton moveSwitch = (ToggleButton) findViewById(R.id.toggle_move_switch);
        final ToggleButton pinSwitch = (ToggleButton) findViewById(R.id.toggle_pin_switch);
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        Toast toast = new Toast(this);
        if(location == null){
            toast.makeText(this, "Cant get current location", Toast.LENGTH_SHORT).show();
        } else if(moveSwitch.getText() == moveSwitch.getTextOn()){
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.animateCamera(update);
        }

        if(pinSwitch.getText() == pinSwitch.getTextOn()){
            if(lastPinnedLL == null){
                lastPinnedLL = new LatLng(latitude, longitude);
                mGoogleMap.addMarker(new MarkerOptions().position(lastPinnedLL).title("Initial Position!"));
            } else {
                float[] results = new float[1];
                Location.distanceBetween(lastPinnedLL.latitude, lastPinnedLL.longitude, latitude, longitude, results);
                if (results[0] >= 5) {
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("More than 5m!"));
                    lastPinnedLL = new LatLng(latitude, longitude);
                }
            }
        }

        toast.cancel();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
}
