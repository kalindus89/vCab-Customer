package com.vcab.vcabcustomer.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.vcab.vcabcustomer.MessagesClass;
import com.vcab.vcabcustomer.R;
import com.vcab.vcabcustomer.call_back_interfaces.IFirebaseDriverInfoListener;
import com.vcab.vcabcustomer.call_back_interfaces.IFirebaseFailedListener;

import java.util.HashMap;
import java.util.Map;

public class HomeFragmentOld extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap googleMap;


    //update current locations
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //load driver
    private double instance=1.0; // default in Km
    private static final double LIMIT_RANGE=10.0; //  Km
    private Location previousLocation,currentLocation; // use to calculate distance

    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;



    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    public HomeFragmentOld() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home_old, container, false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    showMap();
                }
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                // R.id.map is a layout
                transaction.replace(R.id.google_map, mapFragment).commit();

                showMap();
                getLastKnowLocations();
            }
        }, 1500);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showMap() {

        mapFragment.getMapAsync(this);

    }

    private void getLastKnowLocations() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000); // location update time
        locationRequest.setFastestInterval(3000); // location update from the other apps in phone
        locationRequest.setSmallestDisplacement(10f); // Set the minimum displacement between location updates in meters

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                        //  System.out.println("aaaaaaaa2 " + "Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());

                        if (googleMap != null) {
                            //   markOnMap(locationResult.getLastLocation(),16,);

                            //MessagesClass.showToastMsg(locationResult.getLastLocation().toString(), getActivity());

                            LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                            saveDataInFirestore(locationResult);
                        }


                    }
                });

            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper());


    }

    private void saveDataInFirestore(LocationResult locationResult) {


        try {
            if (FirebaseAuth.getInstance().getUid() != null) { // when user close the app, uId() gets null. if its null we stop this service

                Map<String, Object> userLocation = new HashMap<>();
                userLocation.put("geo_point", (new GeoPoint(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude())));

                FirebaseFirestore.getInstance().collection("users/customers/userData/"+FirebaseAuth.getInstance().getUid()+"/lastKnowLocation").document(FirebaseAuth.getInstance().getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                               // MessagesClass.showToastMsg("Succes",getContext());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MessagesClass.showToastMsg(""+e.getMessage(),getContext());

                    }
                });
            }
        } catch (NullPointerException e) { // when user close the app, uId() gets null. if its null we stop this service
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.vcab_maps_style));
            if (!success) {
                MessagesClass.showToastMsg("style not applied", getActivity());
            }

        } catch (Exception e) {
            MessagesClass.showToastMsg(e.getMessage(), getActivity());
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> { // -> lamda mark replaced new inner methods

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

            }
            fusedLocationProviderClient.getLastLocation()

                    .addOnFailureListener(e ->
                            MessagesClass.showToastMsg(e.getMessage(), getActivity()))

                    .addOnSuccessListener(location -> {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    });
            return true;
        });

        //custom view for my location button in google map
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        layoutParams.setMargins(0, 10, 10, 0);


        markOnMap(new LatLng(6.8649, 79.8997), 16, "Nugegoda", "Nugegoda");

    }

    public void markOnMap(LatLng latLng, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(locationName);
        markerOptions.position(latLng);
        markerOptions.snippet(snippet); //place info's
        // googleMap.addMarker(markerOptions).remove();
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);  //max zoom 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings
        //googleMap.moveCamera(cameraUpdate); //directly show
        // googleMap.animateCamera(cameraUpdate); // moving to position without time

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() { // moving to position with time
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }


}