package andbook.example.locationservice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import util.GpsCheck;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button food, friend, gas, bank;
    private final int PERMISSIONREQUEST_RESULT = 100; // 콜백 호출시 requestcode로 넘어가는 구분자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 상태바 제거

        // 오레오 버전 이상 퍼미션 확인 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                checkPermission(); // 퍼미션 허용 진행 및 GPS 활성화 진행
            else{
                //nothing
            }
        }

        GpsCheck.checkGPS_ON_OFF(MainActivity.this); // GPS 확인 유무
        initView();
    }


    // 액티비티 생명주기 onCreate 에서 버튼 초기화 및 이벤트 셋팅
    private void initView() {
        friend = (Button) findViewById(R.id.myLocationSendFriend);
        food = (Button) findViewById(R.id.myLocationFindFood);
        gas = (Button) findViewById(R.id.myLocationFindGas);
        bank = (Button) findViewById(R.id.myLocationFindBank);

        friend.setOnClickListener(this);
        food.setOnClickListener(this);
        gas.setOnClickListener(this);
        bank.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.myLocationSendFriend:
                // 내 위치 전송 하기 버튼
                Intent send = new Intent(getApplicationContext(), AddressListActivity.class);
                startActivity(send);
                break;
            case R.id.myLocationFindFood:
                // 내 주변 음식점 찾기
                Intent food = new Intent(getApplicationContext(), MyLocationFoodActivity.class);
                startActivity(food);
                break;
            case R.id.myLocationFindGas:
                // 내 주변 주유소 찾기
                Intent gas = new Intent(getApplicationContext(), MyLocationGasActivity.class);
                startActivity(gas);
                break;
            case R.id.myLocationFindBank:
                // 내 주변 은행 찾기
                Intent bank = new Intent(getApplicationContext(), MyLocationBankActivity.class);
                startActivity(bank);
                break;
            default:
                break;
        }
    }


    // 퍼미션 권한 진행 메소드
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // 사용자의 최초 퍼미션 허용을 확인         -true: 사용자 퍼미션 거부 , -false: 사용자 동의 미 필요
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getApplicationContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONREQUEST_RESULT);
            }
            else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONREQUEST_RESULT);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);

        if (requestCode == PERMISSIONREQUEST_RESULT) {
            if (grantResult.length > 0) {
                for (int aGrantResult : grantResult) {
                    if (aGrantResult == PackageManager.PERMISSION_DENIED) {
                        //권한이 하나라도 거부 될 시
                        new AlertDialog.Builder(MainActivity.this)
                                .setCancelable(false)
                                .setIcon(R.mipmap.icon)
                                .setMessage("내위치 문자 전송 서비스 사용을 위해서는 요구 권한을 필수적으로 허용해야 합니다.")
                                .setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                                finish();
                            }
                        }).show();
                        return;
                    }
                }
            }
        }
    }
}
