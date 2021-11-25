package com.vcab.vcabcustomer.fragments;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.L;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.vcab.vcabcustomer.Messages_Common_Class;
import com.vcab.vcabcustomer.R;
import com.vcab.vcabcustomer.call_back_interfaces.IFirebaseDriverInfoListener;
import com.vcab.vcabcustomer.call_back_interfaces.IFirebaseFailedListener;
import com.vcab.vcabcustomer.model.AnimationModel;
import com.vcab.vcabcustomer.model.DriverGeoModel;
import com.vcab.vcabcustomer.model.DriverInfoModel;
import com.vcab.vcabcustomer.model.GeoQueryModel;
import com.vcab.vcabcustomer.retrofit_remote.IGoogleApiInterface;
import com.vcab.vcabcustomer.retrofit_remote.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class HomeFragmentOld extends Fragment implements OnMapReadyCallback, IFirebaseDriverInfoListener, IFirebaseFailedListener {

    SupportMapFragment mapFragment;
    GoogleMap googleMap;


    //update current locations
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //load driver
    private double distance = 1.0; // default in Km
    private static final double LIMIT_RANGE = 10.0; //  Km
    private Location previousLocation, currentLocation; // use to calculate distance

    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;

    private boolean firstTime;
    private String cityName;

    // Disposables,they're useful when e.g. you make a long-running HTTP request
    //CompositeDisposable is just a class to keep all your disposables in the same place to you can dispose all of then at once.
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleApiInterface iGoogleApiInterface; // for api request through retrofit

    //moving marker or car
    private List<LatLng> polylineList;
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat,lng;



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
        compositeDisposable.clear();
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

        v.findViewById(R.id.updateLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDriverLocationManually();
            }
        });

        v.findViewById(R.id.updateLocationBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBackDriverLocationManually();
            }
        });

        return v;
    }

    private void updateDriverLocationManually() {
        try{
            DatabaseReference driversLocationRef = FirebaseDatabase.getInstance().getReference("DriversLocation").child("Nugegoda"); //DriversLocation path
            DatabaseReference currentUserRef =driversLocationRef.child("rwPmUIaCDZOw5oJvSx7mMFXrTxx2");//path inside DriversLocation

            GeoFire  geoFire = new GeoFire(driversLocationRef);

            geoFire.setLocation("rwPmUIaCDZOw5oJvSx7mMFXrTxx2", // add current driver location to firebase database. path same as currentUserRef. otherwise data not delete when app close
                  // 6.87487162505107, 79.89882122584598
                    new GeoLocation(6.87423252292433, 79.9022490889011), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                            if (error!=null){
                                Messages_Common_Class.showToastMsg(error.getMessage(),getActivity());
                            }else {
                                Messages_Common_Class.showToastMsg("updated",getActivity());
                            }

                        }
                    });

        }catch (Exception e){
            Messages_Common_Class.showToastMsg(e.getMessage(),getActivity());
        }
    }

    private void updateBackDriverLocationManually() {
        try{
            DatabaseReference driversLocationRef = FirebaseDatabase.getInstance().getReference("DriversLocation").child("Nugegoda"); //DriversLocation path
            DatabaseReference currentUserRef =driversLocationRef.child("rwPmUIaCDZOw5oJvSx7mMFXrTxx2");//path inside DriversLocation

            GeoFire  geoFire = new GeoFire(driversLocationRef);

            geoFire.setLocation("rwPmUIaCDZOw5oJvSx7mMFXrTxx2", // add current driver location to firebase database. path same as currentUserRef. otherwise data not delete when app close
                  // 6.87487162505107, 79.89882122584598
                    new GeoLocation(6.875638546442585, 79.89238928869797), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                            if (error!=null){
                                Messages_Common_Class.showToastMsg(error.getMessage(),getActivity());
                            }else {
                                Messages_Common_Class.showToastMsg("updated",getActivity());
                            }

                        }
                    });

        }catch (Exception e){
            Messages_Common_Class.showToastMsg(e.getMessage(),getActivity());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showMap() {

        mapFragment.getMapAsync(this);

    }

    private void getLastKnowLocations() {

        iGoogleApiInterface = RetrofitClient.getInstance().create(IGoogleApiInterface.class);

        iFirebaseFailedListener = this;
        iFirebaseDriverInfoListener = this;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

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
                        if (googleMap != null) {
                            //   markOnMap(locationResult.getLastLocation(),16,);

                           /* LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                            //   saveDataInFirestore(locationResult);

                            // if user has changed location, calculate and load drivers again

                            if (firstTime) {
                                previousLocation = currentLocation = locationResult.getLastLocation();
                                firstTime = false;
                            } else {
                                previousLocation = currentLocation;
                                currentLocation = locationResult.getLastLocation();
                            }

                            if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) { //driver within the range
                                loadAvailableDrivers();
                            } else {

                                Messages_Common_Class.showToastMsg("Out of range. Try again", getActivity());

                            }*/
                        }


                    }
                });

            }
        };


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper());

        loadAvailableDrivers();
    }

    private void loadAvailableDrivers() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Messages_Common_Class.showToastMsg(e.getMessage(), getContext());

            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addressList;

                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    cityName = addressList.get(0).getLocality();

                    //Query
                    DatabaseReference driversLocationRef = FirebaseDatabase.getInstance().getReference("DriversLocation").child(cityName); //DriversLocation path

                    GeoFire geoFire = new GeoFire(driversLocationRef); // get all drivers in same city

                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), distance);
                    geoQuery.removeAllListeners();

                    //get all in path
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) { //The location of a key now matches the query criteria.
                            Messages_Common_Class.driverFound.add(new DriverGeoModel(key, location));
                        }

                        @Override
                        public void onKeyExited(String key) { // The location of a key no longer matches the query criteria.

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) { // The location of a key changed but the location still matches the query criteria.

                        }

                        @Override
                        public void onGeoQueryReady() {  //All current data has been loaded from the server and all initial events have been fired.

                            if (distance <= LIMIT_RANGE) {
                                distance++;
                                loadAvailableDrivers(); // continue search drivers in next Km
                            } else {
                                distance = 1.0;
                                addDriversToMap();
                            }

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) { // There was an error while performing this query, e.g. a violation of security rules.

                            Messages_Common_Class.showToastMsg(error.getMessage(), getActivity());

                        }
                    });

                    //listen to new driver in city range, when user is open the app
                    driversLocationRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0), geoQueryModel.getL().get(1)); // lat and long

                            DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(), geoLocation);

                            Location newDriverLocation = new Location("");
                            newDriverLocation.setLatitude(geoLocation.latitude);
                            newDriverLocation.setLongitude(geoLocation.longitude);

                            float newDistance = location.distanceTo(newDriverLocation) / 1000; //in km

                            if (newDistance <= LIMIT_RANGE) {
                                findDriverByKey(driverGeoModel);
                            }

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                } catch (IOException e) {
                    Messages_Common_Class.showToastMsg(e.getMessage(), getActivity());
                }

            }
        });

    }

    private void addDriversToMap() {
        //  System.out.println("aaaaaa ");

        if (Messages_Common_Class.driverFound.size() > 0) {

            Observable.fromIterable(Messages_Common_Class.driverFound)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(driverGeoModel -> {

                findDriverByKey(driverGeoModel);

            }, throwable -> {

                Messages_Common_Class.showSnackBar(throwable.getMessage(), getView());
            }, () -> {
            });


        } else {
            Messages_Common_Class.showSnackBar("Drivers not found", getView());
        }


    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {

        //  Messages_Common_Class.showToastMsg(driverGeoModel.getKey(),getActivity());

        String fireStorePath = "users/drivers/userData/" + driverGeoModel.getKey();

        DocumentReference nycRef = FirebaseFirestore.getInstance().document(fireStorePath);

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        driverGeoModel.setDriverInfoModel(document.toObject(DriverInfoModel.class));
                        iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverGeoModel);
                    }
                } else {
                    iFirebaseFailedListener.onFirebaseLoadFailed("no driver found");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                iFirebaseFailedListener.onFirebaseLoadFailed(e.getMessage());

            }
        });

    }

    private void saveDataInFirestore(LocationResult locationResult) {


        try {
            if (FirebaseAuth.getInstance().getUid() != null) { // when user close the app, uId() gets null. if its null we stop this service

                Map<String, Object> userLocation = new HashMap<>();
                userLocation.put("geo_point", (new GeoPoint(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude())));

                FirebaseFirestore.getInstance().collection("users/customers/userData/" + FirebaseAuth.getInstance().getUid() + "/lastKnowLocation").document(FirebaseAuth.getInstance().getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                // MessagesClass.showToastMsg("Succes",getContext());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Messages_Common_Class.showToastMsg("" + e.getMessage(), getContext());

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
                Messages_Common_Class.showToastMsg("style not applied", getActivity());
            }

        } catch (Exception e) {
            Messages_Common_Class.showToastMsg(e.getMessage(), getActivity());
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
                            Messages_Common_Class.showToastMsg(e.getMessage(), getActivity()))

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


    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        //We already have marker with this key, doesn't set again

        if (!Messages_Common_Class.markerList.containsKey(driverGeoModel.getKey())) {


            int height = 160;
            int width = 75;
            BitmapDrawable bitmapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
            Bitmap b = bitmapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            Messages_Common_Class.markerList.put(driverGeoModel.getKey(),
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(driverGeoModel.getGeoLocation().latitude, driverGeoModel.getGeoLocation().longitude))
                            .flat(true)
                            .title("Driver Information")
                            .snippet("Driver Phone Number")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

        }

        if (!TextUtils.isEmpty(cityName)) {

            DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference("DriversLocation")
                    .child(cityName)
                    .child(driverGeoModel.getKey());

            driverLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (!snapshot.hasChildren()) {
                        //remove car if no record.

                        if (Messages_Common_Class.markerList.get(driverGeoModel.getKey()) != null) {

                            Messages_Common_Class.markerList.get(driverGeoModel.getKey()).remove();//remove marker
                            Messages_Common_Class.markerList.remove(driverGeoModel.getKey());//remove marker info from hash map
                            Messages_Common_Class.driverLocationSubscribe.remove(driverGeoModel.getKey());//remove driver info from hash map
                            driverLocation.removeEventListener(this); // remove event listner
                        }

                    }
                    else {
                      //  System.out.println("aaaaa666");
                        if (Messages_Common_Class.markerList.get(driverGeoModel.getKey()) != null) {

                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            AnimationModel animationModel = new AnimationModel(false,geoQueryModel);

                            if(Messages_Common_Class.driverLocationSubscribe.get(driverGeoModel.getKey()) !=null){

                                Marker currentMaker=Messages_Common_Class.markerList.get(driverGeoModel.getKey());
                                AnimationModel oldPosition = Messages_Common_Class.driverLocationSubscribe.get(driverGeoModel.getKey());

                                //driver old location
                                String from = new StringBuilder().append(oldPosition.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(oldPosition.getGeoQueryModel().getL().get(1))
                                        .toString();

                                //driver new location
                                String to  = new StringBuilder().append(animationModel.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(animationModel.getGeoQueryModel().getL().get(1))
                                        .toString();

                                moveMarkerAnimation(driverGeoModel.getKey(),animationModel,currentMaker,from,to);

                            }else{
                                //First location Init
                                Messages_Common_Class.driverLocationSubscribe.put(driverGeoModel.getKey(),animationModel);
                            }

                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


    }

    private void moveMarkerAnimation(String key, AnimationModel animationModel, Marker currentMaker, String from, String to) {

        if(!animationModel.isRun()){

            //request API
            compositeDisposable.add(iGoogleApiInterface.getDirection("driving",
                    "less_driving",from,to,
                    getString(R.string.google_map_api_key))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(returnResults->{

                       // Log.d("Api_return",returnResults);

                        try {

                            JSONObject jsonObject = new JSONObject(returnResults);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");

                            for(int i=0; i<jsonArray.length();i++){

                                JSONObject route=jsonArray.getJSONObject(i);
                                JSONObject poly = route.getJSONObject("overview_polyline");
                                String polyline=poly.getString("points");

                                polylineList = Messages_Common_Class.decodePoly(polyline);

                            }

                            //Moving
                            handler = new Handler();
                            index=1;
                            next=1;

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {

                                    if (polylineList.size()>1){

                                        if(index<polylineList.size()-2){
                                            index++;
                                            next =index+1;
                                            start =polylineList.get(index);
                                            end=polylineList.get(next);

                                        }
                                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                                        valueAnimator.setDuration(3000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                v = valueAnimator.getAnimatedFraction();
                                                lat = v*end.latitude +(1-v) *start.latitude;
                                                lng = v*end.longitude +(1-v) *start.longitude;

                                                LatLng newPos =new LatLng(lat,lng);

                                                currentMaker.setPosition(newPos);
                                                currentMaker.setAnchor(0.5f,0.5f);
                                                currentMaker.setRotation(Messages_Common_Class.getBearing(start,newPos));
                                            }
                                        });

                                        valueAnimator.start();
                                        if(index<polylineList.size()-2){ // reach destination

                                            handler.postDelayed(this,1500);

                                        }
                                        else if(index<polylineList.size()-1){ // done

                                           animationModel.setRun(false);
                                           Messages_Common_Class.driverLocationSubscribe.put(key,animationModel); //update data

                                        }


                                    }

                                }
                            };

                            //run handler
                            handler.postDelayed(runnable,1500);

                        }catch (Exception e){
                            Log.d("aaaaaaaa",e.getMessage());
                           // Messages_Common_Class.showToastMsg("Error in web service direction",getActivity());
                        }

                    }));


        }


    }

    @Override
    public void onFirebaseLoadFailed(String message) {

        Messages_Common_Class.showSnackBar(message, getView());

    }
}