package org.lineageos.performanceprofile;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.SystemProperties;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import org.lineageos.performanceprofile.R;

public class PerformanceProfileTileService extends TileService {

    private static final String PROPERTY_PERFORMANCE_PROFILE = "sys.thermal.performance_profile";

    @Override
    public void onClick() {
        super.onClick();

        String propValue = SystemProperties.get(PROPERTY_PERFORMANCE_PROFILE, "1");
        int currentProfileIdx = 1;
        try {
            currentProfileIdx = Integer.parseInt(propValue);
        } catch (NumberFormatException ignored) {}
        
        int next = currentProfileIdx + 1;
        if (next > 2) {
            next = 0;
        }

        try {
            SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, Integer.toString(next));
        } catch (Exception e) {
            android.util.Log.e("PerfProfileTile", "System property modification exception", e);
        }

        updateAppIcon(next);
        updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        String propValue = SystemProperties.get(PROPERTY_PERFORMANCE_PROFILE, "1");
        int profileIdx = 1;
        try {
            profileIdx = Integer.parseInt(propValue);
        } catch (NumberFormatException ignored) {}

        int customIconResId;
        String labelText;

        switch (profileIdx) {
            case 0:
                customIconResId = R.mipmap.lightweight_profile;
                labelText = "Perf: Light";
                break;
            case 2:
                customIconResId = R.mipmap.performance_profile;
                labelText = "Perf: Max";
                break;
            case 1:
            default:
                customIconResId = R.mipmap.stock_profile;
                labelText = "Perf: Stock";
                break;
        }

        tile.setIcon(Icon.createWithResource(this, customIconResId));
        tile.setLabel(labelText);
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    private void updateAppIcon(int profileIdx) {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        String targetAlias;

        switch (profileIdx) {
            case 0:  targetAlias = packageName + ".LauncherLow"; break;
            case 2:  targetAlias = packageName + ".LauncherHigh"; break;
            case 1:
            default: targetAlias = packageName + ".LauncherMedium"; break;
        }

        String lowPath = packageName + ".LauncherLow";
        String medPath = packageName + ".LauncherMedium";
        String hiPath = packageName + ".LauncherHigh";

        try {
            if (!lowPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(this, lowPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!medPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(this, medPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (!hiPath.equals(targetAlias)) {
                pm.setComponentEnabledSetting(new ComponentName(this, hiPath), 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            pm.setComponentEnabledSetting(new ComponentName(this, targetAlias), 
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            android.util.Log.e("PerfProfile", "Background alias switch failure", e);
        }
    }
}
