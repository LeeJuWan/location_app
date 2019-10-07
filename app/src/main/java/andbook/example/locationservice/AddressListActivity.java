package andbook.example.locationservice;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import adapter.AddressAdapter;
import dto.AddressDTO;
import util.GpsCheck;

public class AddressListActivity extends AppCompatActivity {
    // 보내고자하는 사용자 번호 변수, 사용자 이름 변수
    private String select_number = "" , select_name = "";

    // 리스트 뷰 관련 변수
    private ArrayList<AddressDTO> address;
    private AddressAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresslist);

        Button select_Btn = (Button) findViewById(R.id.contact_addr);
        listView = (ListView) findViewById(R.id.address_list);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        GpsCheck.checkGPS_ON_OFF(AddressListActivity.this); // GPS 확인 유무

        select_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContectList();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // 선택한 전화번호 가져오기
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select_number = adapter.getItem(position).getPhone();
                select_name = adapter.getItem(position).getName();

                new AlertDialog.Builder(AddressListActivity.this,R.style.AlertDialog)
                        .setCancelable(false)
                        .setIcon(R.mipmap.icon)
                        .setTitle("내 위치 전송")
                        .setMessage(select_name + "님에게 위치 전송 하시겠습니까?")
                        .setPositiveButton("전송", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mylocation();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(AddressListActivity.this, "전송 취소", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    // 주소록 가져온 뒤, list View로 나타냄
    private void getContectList(){

        address = new ArrayList<AddressDTO>();
        Cursor cursor = getContentResolver().query( // 사용자의 주소록을 provider 접근하여 주소록 read 진행
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                address.add(new AddressDTO(name, phone)); // 반복문에 따라 계속 주소록 add
            }
            cursor.close();
            // 전화 번호 주소록 모은 후에 어댑터에서 리스트로 연결
            adapter = new AddressAdapter(AddressListActivity.this, address);
            listView.setAdapter(adapter);
        } else
            Toast.makeText(getApplicationContext(), "주소록 에러입니다.", Toast.LENGTH_SHORT).show();
    }

    // 내 위치 전송 기능 - 위치 수신1 (샐룰러방식 , Network AP방식 , GPS방식)
    private void mylocation() {

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null){
            //NULL POINTER EXCEPTION 방지
            Toast.makeText(getApplicationContext(), "내 위치 전송 진행 중", Toast.LENGTH_SHORT).show();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,// 등록할 위치 제공자
                        1000, // 통지사이의 최소 시간 간격
                        1, // 통지사이의 최소 변경 거리
                        locationListener_SMS);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,// 등록할 위치 제공자
                        1000, // 통지사이의 최소 시간 간격
                        1, // 통지사이의 최소 변경 거리
                        locationListener_SMS);
            } catch (SecurityException e) {
                System.err.println("security error 발생");
            }
        }
        else // 전송 에러 알림
            Toast.makeText(getApplicationContext(),"SMS 전송 ERROR",Toast.LENGTH_SHORT).show();
    }
    // 내 위치 전송 기능 위치 수신 2 및 문자 전송 진행 (위치 수신 리스너)
    private LocationListener locationListener_SMS = new LocationListener() {

        // 위치값이 갱신되면 이벤트 발생
        // 위치 제공자 GPS:위성 수신으로 정확도가 높다, 실내사용 불가,위치 제공자 Network:인터넷 엑세스 수신으로 정확도가 아쉽다, 실내 사용 가능
        @Override
        public void onLocationChanged(Location location) {
            List<Address> addresses_SMS = null;
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            SmsManager smsManager = SmsManager.getDefault();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            try {
                addresses_SMS = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                System.err.println("IOException error 발생");
            }
            String sendMassage = "";
            if (addresses_SMS == null)
                sendMassage = "현재 위치: " + location.getLongitude() + " , " + location.getLatitude() + "입니다 !";
            else
                sendMassage = "현재 위치: " + addresses_SMS.get(0).getAddressLine(0) + "입니다 !";

            smsManager.sendTextMessage(select_number, null, sendMassage, null, null); // 리스트 선택에서 받아온 번호로 전송
            Toast.makeText(getApplicationContext(), "내 위치 문자 전송완료", Toast.LENGTH_SHORT).show();

            if (locationManager != null)
                locationManager.removeUpdates(locationListener_SMS); // 위치 전송후 리소스 소모 방지를 위해 remove 진행
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //
        }
        @Override
        public void onProviderEnabled(String provider) {
            //
        }
        @Override
        public void onProviderDisabled(String provider) {
            //
        }
    };
}
