package util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import andbook.example.locationservice.R;

public class GpsCheck {
    // 메모리에 고정해서 사용 / 모든 activity에서 GPS 확인 진행

    private static LocationManager locationManagerGPS;

    public static void checkGPS_ON_OFF(final Activity activity) {

        locationManagerGPS = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);

        if(locationManagerGPS != null){
            // NULL POINT EXCEPTION 제거
            if (!locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // GPS 설정 off일 시 설정화면으로 이동
                new AlertDialog.Builder(activity, R.style.AlertDialog)
                        .setCancelable(false)
                        .setIcon(R.mipmap.icon)
                        .setTitle("GPS 미설정")
                        .setMessage("GPS가 미설정 되어 서비스 제공에 어려움이 있으니 GPS 사용을 허용해주세요\n" +
                                "허용한 뒤, 원활한 사용을 위해 앱을 다시 켜주십시오.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent setting = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                setting.addCategory(Intent.CATEGORY_DEFAULT);
                                activity.startActivity(setting);
                                activity.finish();
                            }
                        }).show();
            }
            else{
                // Noting
            }
        }
        else // 에러 알림
            Toast.makeText(activity,"GPS NULL ERROR",Toast.LENGTH_SHORT).show();
    }
}
