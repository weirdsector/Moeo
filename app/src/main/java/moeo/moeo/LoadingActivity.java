package moeo.moeo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.google.android.gms.common.util.SharedPreferencesUtils;


public class LoadingActivity extends AppCompatActivity {
    private Handler mHandler;
    private Runnable mRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences pref = getSharedPreferences("IS_FIRST",MODE_PRIVATE);
                if(pref.getBoolean("isFirst",true)) {
                    pref.edit().putBoolean("isFirst",false).commit();
                    Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable,3000);

    }

}
