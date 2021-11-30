package com.vcab.vcabcustomer;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.ui.IconGenerator;
import com.vcab.vcabcustomer.databinding.ActivityRequestDriverBinding;
import com.vcab.vcabcustomer.model.DriverGeoModel;
import com.vcab.vcabcustomer.model.SelectPlaceEvent;
import com.vcab.vcabcustomer.retrofit_remote.IGoogleApiInterface;
import com.vcab.vcabcustomer.retrofit_remote.RetrofitClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RequestDriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRequestDriverBinding binding;
    private SelectPlaceEvent selectPlaceEvent;

    // Disposables,they're useful when e.g. you make a long-running HTTP request
    //CompositeDisposable is just a class to keep all your disposables in the same place to you can dispose all of then at once.
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleApiInterface iGoogleApiInterface; // for api request through retrofit
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions grayPolylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    TextView txt_origin;

    Button btn_confirm_vcab,btn_confirm_pickup;
    CardView confirm_cab_layout,confirm_pickup_layout,find_your_driver_layout;
    TextView txt_address_pickup;
    View fill_maps;
    RelativeLayout main_request_layout;

    private Marker originMarker, destinationMarker;

    //effect
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPlusAnimator;

    //slowly camera spinning;
    private ValueAnimator animatorCam;
    private static final int DESIRED_NUM_OF_SPINS=5;
    private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN=40;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class)) {
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);
            EventBus.getDefault().unregister(this);

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event) {
        selectPlaceEvent = event;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRequestDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        confirm_cab_layout=findViewById(R.id.confirm_cab_layout);
        confirm_pickup_layout=findViewById(R.id.confirm_pickup_layout);
        txt_address_pickup=findViewById(R.id.txt_address_pickup);
        fill_maps=findViewById(R.id.fill_maps);
        find_your_driver_layout=findViewById(R.id.find_your_driver_layout);
        main_request_layout=findViewById(R.id.main_request_layout);

        btn_confirm_vcab=findViewById(R.id.btn_confirm_vcab);
        btn_confirm_pickup=findViewById(R.id.btn_confirm_pickup);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

        btn_confirm_vcab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm_cab_layout.setVisibility(View.GONE);
                confirm_pickup_layout.setVisibility(View.VISIBLE);

                setDataPickup();
            }
        });

        btn_confirm_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 if(mMap ==null) return;
                 if(selectPlaceEvent ==null) return;

                 //clear map
                mMap.clear();

                // camera angle
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(selectPlaceEvent.getUserOrigin())
                        .tilt(45f)
                        .zoom(16f)
                        .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //startAnimation
                addMarkerWithPlusAnimation();


            }
        });

    }

    private void addMarkerWithPlusAnimation() {

        confirm_pickup_layout.setVisibility(View.GONE);
        fill_maps.setVisibility(View.VISIBLE); // makes map more darker
        find_your_driver_layout.setVisibility(View.VISIBLE); // makes map more darker

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectPlaceEvent.getUserOrigin()));

        addPulsatingEffect(selectPlaceEvent.getUserOrigin());

    }



    private void addPulsatingEffect(LatLng userOrigin) {

        if(lastPlusAnimator !=null)lastPlusAnimator.cancel();
        if(lastUserCircle !=null)lastUserCircle.setCenter(userOrigin);

        //add circle to marker
        lastPlusAnimator =Messages_Common_Class.valueAnimate(duration,animation->{
            if(lastUserCircle !=null)lastUserCircle.setRadius((float)animation.getAnimatedValue());
            else{
                lastUserCircle =mMap.addCircle(new CircleOptions()
                .center(userOrigin)
                .radius((Float)animation.getAnimatedValue())
                .strokeColor(Color.WHITE).fillColor(Color.parseColor("#33333333"))
                );
            }

        });

        startMapCameraSpinningAnimation(userOrigin);

    }

    private void startMapCameraSpinningAnimation(LatLng target) {

        if(animatorCam !=null) animatorCam.cancel();
        animatorCam=ValueAnimator.ofFloat(0,DESIRED_NUM_OF_SPINS*360);
        animatorCam.setDuration(DESIRED_SECONDS_PER_ONE_FULL_360_SPIN*DESIRED_NUM_OF_SPINS*1000);
        animatorCam.setInterpolator(new LinearInterpolator());
        animatorCam.setStartDelay(100);
        animatorCam.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                Float newBearingValue=(Float) valueAnimator.getAnimatedValue();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(target)
                .zoom(16f)
                .tilt(45f)
                .bearing(newBearingValue)
                .build()));
            }
        });
        animatorCam.start();

        findNearByDriver(target);

    }

    private void findNearByDriver(LatLng target) {

        if(Messages_Common_Class.driverFound.size()>0){

            float min_distance=0;

            Location currentCustomerLocation = new Location("");
            currentCustomerLocation.setLatitude(target.latitude);
            currentCustomerLocation.setLongitude(target.longitude);

            DriverGeoModel foundDriver= Messages_Common_Class.driverFound.get(Messages_Common_Class.driverFound.keySet().iterator().next());// default driver. first driver in list

            for(String key:Messages_Common_Class.driverFound.keySet()){

                Location driverLocation = new Location("");
                driverLocation.setLatitude(Messages_Common_Class.driverFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Messages_Common_Class.driverFound.get(key).getGeoLocation().longitude);

                //compare 2 location

                if(min_distance==0){
                    min_distance=driverLocation.distanceTo(currentCustomerLocation);
                    foundDriver=Messages_Common_Class.driverFound.get(key);
                }

                else if(driverLocation.distanceTo(currentCustomerLocation)<min_distance){
                    // if have any driver smaller min_distance, then get that one
                    min_distance=driverLocation.distanceTo(currentCustomerLocation);
                    foundDriver=Messages_Common_Class.driverFound.get(key);
                }

               // Messages_Common_Class.showSnackBar("Driver found: "+foundDriver.getDriverInfoModel().getPhone(),main_request_layout);
                Messages_Common_Class.sendRequestToDriver(this,main_request_layout,foundDriver,target);
            }

        }else{
            Messages_Common_Class.showSnackBar("No driver found",main_request_layout);
        }
    }

    @Override
    protected void onDestroy() {
        if(animatorCam!=null) animatorCam.end();
        super.onDestroy();
    }

    private void setDataPickup() {

        txt_address_pickup.setText(txt_origin !=null ? txt_origin.getText() : "None");
        mMap.clear();// clear all map markers

        addPickupMarker();
    }

    private void addPickupMarker() {

        View view = getLayoutInflater().inflate(R.layout.pickup_info_windows, null);

        //create icon for marker
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentView(view);
        iconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = iconGenerator.makeIcon();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getUserOrigin()));


    }

    private void init() {
        iGoogleApiInterface = RetrofitClient.getInstance().create(IGoogleApiInterface.class);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.vcab_maps_style));
            if (!success) {
                Messages_Common_Class.showToastMsg("style not applied", this);
            }

        } catch (Exception e) {
            Messages_Common_Class.showToastMsg(e.getMessage(), this);
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> { // -> lamda mark replaced new inner methods

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectPlaceEvent.getUserOrigin(), 18f));// client location
            return true;
        });

        //custom view for my location button in google map
        View locationButton = ((View) findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        layoutParams.setMargins(0, 10, 10, 0);


        drawPath(selectPlaceEvent);


    }

    private void drawPath(SelectPlaceEvent selectPlaceEvent) {
        compositeDisposable.add(iGoogleApiInterface.getDirection("driving",
                "less_driving", selectPlaceEvent.getUserOriginString(), selectPlaceEvent.getUserDestinationString(),
                getString(R.string.google_map_api_key))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResults -> {

                    // Log.d("Api_return",returnResults);

                    try {

                        JSONObject jsonObject = new JSONObject(returnResults);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Messages_Common_Class.decodePoly(polyline);

                        }
                        //polyline animation. black to gray

                        //grey polyline animations
                        grayPolylineOptions = new PolylineOptions();
                        grayPolylineOptions.color(Color.GRAY);
                        grayPolylineOptions.width(12);
                        grayPolylineOptions.startCap(new SquareCap());
                        grayPolylineOptions.jointType(JointType.ROUND);
                        grayPolylineOptions.addAll(polylineList);

                        greyPolyline = mMap.addPolyline(grayPolylineOptions);

                        //black polyline animations
                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polylineList);

                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        // car moving animator
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                        valueAnimator.setDuration(1100);// car moving time from one location to another
                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator value) {

                                List<LatLng> points = greyPolyline.getPoints();
                                int percentValue = (int) value.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyline.setPoints(p);

                            }
                        });

                        valueAnimator.start();

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(selectPlaceEvent.getUserOrigin())
                                .include(selectPlaceEvent.getUserDestination())
                                .build();

                        //add car icon for origin

                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs"); // In this arrya has information about distance, end_location,start_address,start_location and many
                        //https://developers.google.com/maps/documentation/directions/get-directions#DirectionsLeg
                        JSONObject legObject = legs.getJSONObject(0);

                        String start_address = legObject.getString("start_address");
                        String end_address = legObject.getString("end_address");

                        JSONObject time = legObject.getJSONObject("duration");
                        String duration = time.getString("text");

                        addOriginMarker(duration, start_address);
                        addDestinationMarker(end_address);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));


                    } catch (Exception e) {
                        Log.d("aaaaaaaaerr", e.getMessage());
                        // Messages_Common_Class.showToastMsg("Error in web service direction",getActivity());
                    }

                }));


    }

    private void addOriginMarker(String duration, String start_address) {

        View view = getLayoutInflater().inflate(R.layout.origin_info_windows, null);

        TextView txt_time = (TextView) view.findViewById(R.id.txt_time);
        txt_origin = (TextView) view.findViewById(R.id.txt_origin);

        txt_time.setText(Messages_Common_Class.formatDuration(duration));
        txt_origin.setText(Messages_Common_Class.formatAddress(start_address));

        //create icon for marker
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentView(view);
        iconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = iconGenerator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getUserOrigin()));


    }

    private void addDestinationMarker(String end_address) {

        View view = getLayoutInflater().inflate(R.layout.destination_info_windows, null);

        TextView txt_destination = (TextView) view.findViewById(R.id.txt_destination);
        txt_destination.setText(Messages_Common_Class.formatAddress(end_address));

        //create icon for marker
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentView(view);
        iconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = iconGenerator.makeIcon();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getUserDestination()));


    }
}