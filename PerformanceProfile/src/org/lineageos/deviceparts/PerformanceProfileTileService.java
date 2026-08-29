/*
 * Copyright (C) 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.deviceparts;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.quicksettings.TileService;

public class PerformanceProfileTileService extends TileService {
    private static final String LAUNCHER_ALIAS =
            "org.lineageos.deviceparts.PerformanceProfileLauncher";

    @Override
    public void onClick() {
        super.onClick();

        Intent intent = new Intent(this, PerformanceProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

        startActivityAndCollapse(pendingIntent);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        setLauncherAliasEnabled(false);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        setLauncherAliasEnabled(false);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        setLauncherAliasEnabled(true);
    }

    private void setLauncherAliasEnabled(boolean enabled) {
        ComponentName launcherAlias = new ComponentName(
                this,
                LAUNCHER_ALIAS);

        getPackageManager().setComponentEnabledSetting(
                launcherAlias,
                enabled
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
