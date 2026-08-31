/*
 * Copyright (C) 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://apache.org
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.performanceprofile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.service.quicksettings.TileService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformanceProfileActivity extends Activity 
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Spinner mSpinner;
    private Button mDoneButton;
    private boolean mIsInitialSelect = true;
    private SharedPreferences mPrefs;
    private static final String ACTION_SET_PROFILE = "org.lineageos.performanceprofile.ACTION_SET_PROFILE";
    private static final String BACKEND_PACKAGE = "org.lineageos.performanceprofile.backend";
    private static final String BACKEND_RECEIVER = "org.lineageos.performanceprofile.backend.ProfileCommandReceiver";
    private static final String BACKEND_TILE_SERVICE = "org.lineageos.performanceprofile.backend.PerformanceProfileTileService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(60, 60, 60, 60);
        rootLayout.setGravity(Gravity.CENTER);

        TextView titleText = new TextView(this);
        titleText.setText("Select Performance Profile");
        titleText.setTextSize(20);
        titleText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 50);
        rootLayout.addView(titleText, titleParams);

        mSpinner = new Spinner(this);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        spinnerParams.setMargins(0, 0, 0, 60);
        rootLayout.addView(mSpinner, spinnerParams);

        mDoneButton = new Button(this);
        mDoneButton.setText("Done");
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rootLayout.addView(mDoneButton, doneParams);

        setContentView(rootLayout);

        List<String> profiles = new ArrayList<>();
        profiles.add("Lightweight");
        profiles.add("Stock");
        profiles.add("Performance");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, profiles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        int initialIndex = mPrefs.getInt("active_profile", 1);
        mSpinner.setSelection(initialIndex);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int savedProfile = mPrefs.getInt("active_profile", 1);

                if (position == savedProfile) {
                    return;
                }
                
                mPrefs.edit().putInt("active_profile", position).apply();

                // FORWARD INTEGRATION: Delegate hardware property changes securely to the backend
                try {
                    Intent commandIntent = new Intent(ACTION_SET_PROFILE);
                    commandIntent.setComponent(new ComponentName(BACKEND_PACKAGE, BACKEND_RECEIVER));
                    commandIntent.putExtra("profile_index", position);
                    sendBroadcast(commandIntent);
                } catch (Exception e) {
                    // Shield thread limits
                }
                
                updateAppIcon(position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ComponentName tileComponent = new ComponentName(BACKEND_PACKAGE, BACKEND_TILE_SERVICE);
                    TileService.requestListeningState(PerformanceProfileActivity.this, tileComponent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        mDoneButton.setOnClickListener(v -> finishAndRemoveTask());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        
        mIsInitialSelect = true;
        mSpinner.setSelection(mPrefs.getInt("active_profile", 1));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("active_profile".equals(key)) {
            runOnUiThread(() -> {
                int currentProfile = sharedPreferences.getInt("active_profile", 1);
                if (mSpinner != null && mSpinner.getSelectedItemPosition() != currentProfile) {
                    mIsInitialSelect = true; 
                    mSpinner.setSelection(currentProfile);
                    updateAppIcon(currentProfile);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    /**
     * Toggles the active <activity-alias> launcher component matching the selected profile index.
     */
    public void updateAppIcon(int profileIdx) {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
    
        String targetAlias;
        switch (profileIdx) {
            case 0:
                targetAlias = packageName + ".LauncherLow";
                break;
            case 2:
                targetAlias = packageName + ".LauncherHigh";
                break;
            case 1:
            default:
                targetAlias = packageName + ".LauncherMedium";
                break;
        }
    
        List<String> aliases = Arrays.asList(
            packageName + ".LauncherLow",
            packageName + ".LauncherMedium",
            packageName + ".LauncherHigh"
        );
    
        for (String alias : aliases) {
            ComponentName componentName = new ComponentName(this, alias);
            int newState = alias.equals(targetAlias) 
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED 
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    
            pm.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            );
        }
    }
}
