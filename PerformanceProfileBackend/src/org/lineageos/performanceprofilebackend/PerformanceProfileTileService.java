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

package org.lineageos.performanceprofilebackend;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import org.lineageos.performanceprofilebackend.R;

import java.util.Arrays;
import java.util.List;

public class PerformanceProfileTileService extends TileService {

    private static final String PROPERTY_PERFORMANCE_PROFILE = "persist.vendor.performance_profile";
    private SharedPreferences prefs;

    @Override
    public void onClick() {
        super.onClick();

        prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        
        // RESOLVED: Use a single, distinct integer variable for calculation loops
        int currentProfileIdx = prefs.getInt("active_profile", 1); // Defaults to 1 (Stock)
        
        int next = currentProfileIdx + 1;
        if (next > 2) next = 0;

        prefs.edit().putInt("active_profile", next).apply();
        
        try {
            SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, Integer.toString(next));
        } catch (Exception e) {
            android.util.Log.e("PerfProfileBackend", "SELinux blocked property write!", e);
        }
                
        updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        
        SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("tile_added_by_user", false)) {
            prefs.edit().putBoolean("tile_added_by_user", true).apply();
        }

        updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("tile_added_by_user", false).apply();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        SharedPreferences prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
        int profileIdx = prefs.getInt("active_profile", 1); // Defaults to 1 (Stock)

        int customIconResId;
        String labelText;

        switch (profileIdx) {
            case 0: // LIGHTWEIGHT (LOW FUEL GAUGE)
                customIconResId = R.mipmap.lightweight_profile;
                labelText = "Perf: Light";
                break;
            case 1: // STOCK (MED FUEL GAUGE - BASE DEFAULT)
            default:
                customIconResId = R.mipmap.stock_profile;
                labelText = "Perf: Stock";
                break;
            case 2: // PERFORMANCE (HIGH FUEL GAUGE)
                customIconResId = R.mipmap.performance_profile;
                labelText = "Perf: Max";
                break;
        }

        tile.setIcon(Icon.createWithResource(this, customIconResId));
        tile.setLabel(labelText);
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }
}
