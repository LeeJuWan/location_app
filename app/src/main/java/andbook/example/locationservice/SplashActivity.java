package andbook.example.locationservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceStat) {
        super.onCreate(savedInstanceStat);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 상태바 제거
        try {
            Thread.sleep(2000);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (InterruptedException e) {
            Log.i("SplashActivty", "InterruptedException error");
        }
    }
}
