package org.lineageos.performanceprofile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class PerformanceProfileDialogActivity extends Activity {

    private Button mThrottlingButton;
    private static final String PROPERTY_PERFORMANCE_CPU = "sys.thermal.performance_cpu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context storageContext = createDeviceProtectedStorageContext();
        SharedPreferences prefs = storageContext.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        
        int cpuState = prefs.getInt("cpu_throttling_state", 1);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 60, 60, 60);
        root.setGravity(Gravity.CENTER);

        mThrottlingButton = new Button(this);
        mThrottlingButton.setText("Performance Level CPU Throttling");
        mThrottlingButton.setTextSize(16);
        updateButtonVisuals(cpuState);

        mThrottlingButton.setOnClickListener(v -> {
            int currentState = prefs.getInt("cpu_throttling_state", 1);
            int nextState = (currentState == 1) ? 0 : 1;
            
            updateButtonVisuals(nextState);

            try {
                SystemProperties.set(PROPERTY_PERFORMANCE_CPU, Integer.toString(nextState));
                prefs.edit().putInt("cpu_throttling_state", nextState).commit();
            } catch (Exception e) {
                android.util.Log.e("PerfProfileDialog", "Direct write failed", e);
            }
        });

        root.addView(mThrottlingButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    private void updateButtonVisuals(int state) {
        if (state == 1) {
            mThrottlingButton.setBackgroundColor(Color.parseColor("#4CAF50"));
            mThrottlingButton.setTextColor(Color.WHITE);
        } else {
            mThrottlingButton.setBackgroundColor(Color.parseColor("#F44336"));
            mThrottlingButton.setTextColor(Color.WHITE);
        }
    }
}
