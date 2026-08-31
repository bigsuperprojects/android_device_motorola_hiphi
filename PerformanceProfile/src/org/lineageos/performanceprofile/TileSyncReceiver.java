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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class TileSyncReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("org.lineageos.performanceprofile.ACTION_SYNC_ICON".equals(intent.getAction())) {
            int profileIdx = intent.getIntExtra("profile_index", 1);

            // Synchronize frontend storage values
            SharedPreferences prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("active_profile", profileIdx).apply();

            // Fire the asset shift sequence directly inside unprivileged context
            try {
                PerformanceProfileActivity activity = new PerformanceProfileActivity();
                // Alternatively wrapper context execution mappings safely
                Intent activityIntent = new Intent(context, PerformanceProfileActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                // Directly switch alias mappings to clear cache tracks
                java.lang.reflect.Method updateMethod = PerformanceProfileActivity.class.getDeclaredMethod(
                    "updateAppIcon", int.class
                );
                // Execute using temporary validation instances
                PerformanceProfileActivity dummy = new PerformanceProfileActivity();
                // Pass system context structures
                Context appCtx = context.getApplicationContext();
                // Let the primary method logic update parameters natively
                android.content.pm.PackageManager pm = appCtx.getPackageManager();
                String packageName = appCtx.getPackageName();
                
                String targetAlias;
                switch (profileIdx) {
                    case 0:  targetAlias = packageName + ".LauncherLow"; break;
                    case 2:  targetAlias = packageName + ".LauncherHigh"; break;
                    case 1:
                    default: targetAlias = packageName + ".LauncherMedium"; break;
                }
                
                java.util.List<String> aliases = java.util.Arrays.asList(
                    packageName + ".LauncherLow", packageName + ".LauncherMedium", packageName + ".LauncherHigh"
                );
                
                for (String alias : aliases) {
                    android.content.ComponentName name = new android.content.ComponentName(appCtx, alias);
                    int state = alias.equals(targetAlias) 
                        ? android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED 
                        : android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    pm.setComponentEnabledSetting(name, state, android.content.pm.PackageManager.DONT_KILL_APP);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
