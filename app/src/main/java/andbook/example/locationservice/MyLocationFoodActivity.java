package andbook.example.locationservice;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.os.Looper;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;
import util.GpsCheck;
import util.getApiKey;



public class MyLocationFoodActivity extends AppCompatActivity implements
        LocationListener, OnMapReadyCallback, PlacesListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    private LocationRequest locationRequest;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private LatLng currentPosition;
    private Marker current_marker =null;
    private List<Marker> previous_marker = null;

    @Override
    protected void onPause(){
        super.onPause();
        // 현재 위치 따오지 않음
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            fusedLocationProviderClient = null;
                        }
                    });
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        if (googleMap!=null)
            googleMap.setMyLocationEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfood);

        Button connect_RESTAURANT_Btn = (Button) findViewById(R.id.contact_food);

        GpsCheck.checkGPS_ON_OFF(MyLocationFoodActivity.this); // GPS 확인 유무

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 상태바 제거

        // 사용자 현재 위치 요청 업데이트
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // 전력소모와 위치 정확도의 밸런스 고려
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        // 사용자의 현재 위치 표시하기
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        // 구글맵 현재 위치 띄우기
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.foodMap);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        // 구글맵에 찍을 마커 Array 생성
        previous_marker = new ArrayList<Marker>();

        connect_RESTAURANT_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.clear(); // 지도 클리어

                if (previous_marker != null)
                    previous_marker.clear(); // 지역정보 마커 클리어

                new NRPlaces.Builder()
                        .listener(MyLocationFoodActivity.this)
                        .latlng(currentPosition.latitude,currentPosition.longitude)//현재 위치
                        .radius(2000) // 2000 미터 내에서 검색
                        .type(PlaceType.RESTAURANT) // 음식점
                        .key(getApiKey.place_api_key.trim())
                        .language("ko", "KR")
                        .build()
                        .execute();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        googleMap = map;
        // 현재 위치를 나타내기 위해 locationRequest: 사용자 위치 업데이트 완료
        // locationcallback: 현재 위치에 대한 콜백 메서드
        // Looper: 현재 처리할 쓰레드(비동기 처리)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(),"runnig...",Toast.LENGTH_SHORT).show();
            }
        });
    }
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            super.onLocationResult(locationResult);
            Log.i("진입 callback","Test");
            List<Location> locationList = locationResult.getLocations();

            if(locationList.size() > 0 )
                location = locationList.get(locationList.size()-1);

            currentPosition =
                    new LatLng(location.getLatitude(),location.getLongitude());
            String markerTitle = getCurrentAddress(currentPosition);
            setCurrentLocation(location,markerTitle);

        }
    };

    private void setCurrentLocation(Location location,String markerTitle){
        if (current_marker != null)
            current_marker.remove(); // 현재 마커 제거

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude()); // 위치 따오기

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.draggable(true);

        current_marker = googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        googleMap.moveCamera(cameraUpdate);
    }

    // 현재 위치 주소 불러오기
    private String getCurrentAddress(LatLng latlng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            // 네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        else {
            Address address = addresses.get(0);
            return address.getAddressLine(0); // 현재 위치 한글로 변환 하여 전달
        }
    }

    // 일정시간 && 일정거리 변화 할시 호출 되는 콜백 메서드
    @Override
    public void onLocationChanged(Location location) {
        Log.i("진입 위치 변화","변화");
        currentPosition = new LatLng( location.getLatitude(), location.getLongitude() );
        String errorMessage = "";

        if (current_marker != null )
            current_marker.remove();

        //현재 위치에 마커 생성
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("현재위치");

        current_marker = googleMap.addMarker(markerOptions);

        //지도 상에서 보여주는 영역 이동
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        googleMap.getUiSettings().setCompassEnabled(true);

        //지오코더 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        }
        catch (IOException ioException) {
            errorMessage = "지오코더 서비스 사용불가";
            Toast.makeText( this, errorMessage, Toast.LENGTH_LONG).show();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "잘못된 GPS 좌표";
            Toast.makeText( this, errorMessage, Toast.LENGTH_LONG).show();
        }

        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty())
                errorMessage = "주소 미발견";;
            Toast.makeText( this, errorMessage, Toast.LENGTH_LONG).show();
        }
        else {
            Address address = addresses.get(0);
            Toast.makeText( this, address.getAddressLine(0), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {
        Log.i("진입 PlacesFailure", "onPlacesFailure()");
    }

    @Override
    public void onPlacesStart() {
        Log.i("진입 PlacesStart", "onPlacesStart()");
    }

    // 장소에대한 검색이 성공적으로 이뤄지면 여러장소 마커 띄우기 위한 콜백 메서드
    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        Log.i("진입 PlacesSuccess", "onPlacesSuccess()");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude(),
                            place.getLongitude());

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    Marker item = googleMap.addMarker(markerOptions);
                    previous_marker.add(item);
                }
                // 중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
            }
        });
    }
    @Override
    public void onPlacesFinished() {
        Log.i("진입 PlacesFinished", "onPlacesFinished()");
    }
}