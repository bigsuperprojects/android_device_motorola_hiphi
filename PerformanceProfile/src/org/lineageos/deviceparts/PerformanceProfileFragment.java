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

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PerformanceProfileFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_ADD_QUICK_SETTINGS_TILE =
            "add_quick_settings_tile";

    private static final String KEY_PERFORMANCE_PROFILE =
            "performance_profile";

    private static final String PROPERTY_PERFORMANCE_PROFILE =
            "persist.vendor.performance_profile";

    private static final String LAUNCHER_ALIAS =
            "org.lineageos.deviceparts.PerformanceProfileLauncher";

    private ListPreference mPerformanceProfile;

    @Override
    public void onCreatePreferences(
            @Nullable Bundle savedInstanceState,
            @Nullable String rootKey) {
        setPreferencesFromResource(
                R.xml.performance_profile_preferences,
                rootKey);

        initPerformanceProfilePreference();
        initAddTilePreference();

        if (savedInstanceState == null && isLauncherAliasEnabled()) {
            requestQuickSettingsTile();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePerformanceProfilePreference();
    }

    private void initPerformanceProfilePreference() {
        mPerformanceProfile = findPreference(KEY_PERFORMANCE_PROFILE);

        if (mPerformanceProfile == null) {
            return;
        }

        mPerformanceProfile.setOnPreferenceChangeListener(this);
        updatePerformanceProfilePreference();
    }

    private void initAddTilePreference() {
        Preference addTilePreference =
                findPreference(KEY_ADD_QUICK_SETTINGS_TILE);

        if (addTilePreference == null) {
            return;
        }

        addTilePreference.setOnPreferenceClickListener(preference -> {
            requestQuickSettingsTile();
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(
            @NonNull Preference preference,
            Object newValue) {
        String profile = String.valueOf(newValue);

        if (!isValidProfile(profile)) {
            return false;
        }

        SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, profile);
        return true;
    }

    private void updatePerformanceProfilePreference() {
        if (mPerformanceProfile == null) {
            return;
        }

        String profile = SystemProperties.get(
                PROPERTY_PERFORMANCE_PROFILE,
                "");

        if (!isValidProfile(profile)) {
            profile = "0";
            SystemProperties.set(PROPERTY_PERFORMANCE_PROFILE, profile);
        }

        mPerformanceProfile.setValue(profile);
    }

    private void requestQuickSettingsTile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        StatusBarManager statusBarManager =
                requireContext().getSystemService(StatusBarManager.class);

        if (statusBarManager == null) {
            return;
        }

        ComponentName tileComponent = new ComponentName(
                requireContext(),
                PerformanceProfileTileService.class);

        Icon icon = Icon.createWithResource(
                requireContext(),
                R.drawable.ic_performance_profile);

        statusBarManager.requestAddTileService(
                tileComponent,
                getString(R.string.performance_profile_title),
                icon,
                requireContext().getMainExecutor(),
                result -> { });
    }

    private boolean isLauncherAliasEnabled() {
        ComponentName launcherAlias = new ComponentName(
                requireContext(),
                LAUNCHER_ALIAS);

        int state = requireContext()
                .getPackageManager()
                .getComponentEnabledSetting(launcherAlias);

        return state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    private static boolean isValidProfile(String profile) {
        return "0".equals(profile)
                || "1".equals(profile)
                || "2".equals(profile);
    }
}
