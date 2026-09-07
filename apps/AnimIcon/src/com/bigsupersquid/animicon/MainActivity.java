package com.bigsupersquid.animicon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.SystemProperties;

public final class MainActivity extends Activity {
    private static final String TAG = "AnimIcon";
    private static final String PREFS = "state";
    private static final String KEY = "value";
    private static final String KEY_BOOT_TIME = "boot_time";
    private static final String THERMAL_PROFILE_PROPERTY =
        "sys.thermal.perf";

    private static final long INITIALIZATION_DELAY_MS = 15_000L;
    private static final float OVERLAY_Y_OFFSET_FRACTION = -0.17f;

    private static final String[] ALIASES = {
        "com.bigsupersquid.animicon.State0",
        "com.bigsupersquid.animicon.State1",
        "com.bigsupersquid.animicon.State2"
    };

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private FrameLayout overlayContainer;
    private boolean finished;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        long bootTime = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getLong(KEY_BOOT_TIME, 0L);

        long elapsedSinceBoot =
                System.currentTimeMillis() - bootTime;

        if (bootTime > 0L
                && elapsedSinceBoot >= 0L
                && elapsedSinceBoot < INITIALIZATION_DELAY_MS) {
            Toast.makeText(
                    this,
                    "Please wait for initialization",
                    Toast.LENGTH_SHORT)
                    .show();

            finishNow();
            return;
        }

        int oldState = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getInt(KEY, 0);

        int newState = (oldState + 1) % 3;

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putInt(KEY, newState)
                .commit();

try {
    SystemProperties.set(
            THERMAL_PROFILE_PROPERTY,
            Integer.toString(newState));

    Log.i(
            TAG,
            "set " + THERMAL_PROFILE_PROPERTY
                    + "=" + newState);
} catch (RuntimeException exception) {
    Log.e(
            TAG,
            "unable to set "
                    + THERMAL_PROFILE_PROPERTY
                    + "=" + newState,
            exception);
}

        updateAliases(newState);

        Rect bounds = getIntent().getSourceBounds();
        boolean canDrawOverlays = Settings.canDrawOverlays(this);

        if (bounds != null && canDrawOverlays) {
            showOverlay(
                    bounds,
                    animationFor(oldState, newState));
        } else {
            Log.i(
                    TAG,
                    "no overlay: bounds=" + bounds
                            + " canDrawOverlays=" + canDrawOverlays);
            finishNow();
        }
    }

    private int animationFor(int from, int to) {
        if (from == 0 && to == 1) {
            return R.drawable.transition_0_to_1;
        }

        if (from == 1 && to == 2) {
            return R.drawable.transition_1_to_2;
        }

        return R.drawable.transition_2_to_0;
    }

    private void updateAliases(int newState) {
        PackageManager packageManager = getPackageManager();

        packageManager.setComponentEnabledSetting(
                new ComponentName(this, ALIASES[newState]),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        for (int i = 0; i < ALIASES.length; i++) {
            if (i != newState) {
                packageManager.setComponentEnabledSetting(
                        new ComponentName(this, ALIASES[i]),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        }
    }

    private void showOverlay(Rect bounds, int animationId) {
        int size = bounds.width();

        int iconCenterX = bounds.centerX();
        int iconCenterY = bounds.top + (size / 2);

        int overlayX = iconCenterX - (size / 2);
        int overlayY = iconCenterY
                - (size / 2)
                + Math.round(size * OVERLAY_Y_OFFSET_FRACTION);

        Log.i(
                TAG,
                "overlay bounds=" + bounds
                        + " iconSize=" + size
                        + " iconCenter=(" + iconCenterX + ","
                        + iconCenterY + ")"
                        + " position=(" + overlayX + ","
                        + overlayY + ")"
                        + " offsetPx="
                        + Math.round(size * OVERLAY_Y_OFFSET_FRACTION));

        windowManager =
                (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        overlayContainer = new FrameLayout(this);
        overlayContainer.setAlpha(1.0f);

        FrameLayout.LayoutParams childParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView maskView = new ImageView(this);
        maskView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        maskView.setScaleY(-.99f);
        maskView.setScaleX(-.99f);
        maskView.setAlpha(1.0f);
        maskView.setImageResource(R.drawable.static_icon_mask);

        overlayContainer.addView(maskView, childParams);

        ImageView overlayView = new ImageView(this);
        overlayView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        overlayView.setScaleY(-1.0f);
        overlayView.setAlpha(1.0f);

        Drawable drawable = getDrawable(animationId);
        drawable.setAlpha(255);
        overlayView.setImageDrawable(drawable);

        overlayContainer.addView(overlayView, childParams);

WindowManager.LayoutParams params =
        new WindowManager.LayoutParams(
                size,
                size,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBA_8888);

params.alpha = 1.0f;
params.gravity = Gravity.TOP | Gravity.LEFT;
params.x = overlayX;
params.y = overlayY;

        try {
            windowManager.addView(overlayContainer, params);
            Log.i(TAG, "overlay added");
        } catch (RuntimeException exception) {
            Log.e(TAG, "overlay add failed", exception);
            finishNow();
            return;
        }

        if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animation =
                    (AnimatedVectorDrawable) drawable;

            animation.registerAnimationCallback(
                    new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable ignored) {
                            Log.i(TAG, "animation ended");
                            removeOverlayAndFinish();
                        }
                    });

            Log.i(
                    TAG,
                    "animation starting durationTargetMs=2000");

            animation.start();
        } else {
            Log.i(TAG, "drawable is not AnimatedVectorDrawable");

            mainHandler.postDelayed(
                    () -> {
                        Log.i(TAG, "animation fallback fired");
                        removeOverlayAndFinish();
                    },
                    300);
        }

        mainHandler.postDelayed(
                () -> {
                    Log.i(TAG, "animation timeout fallback fired");
                    removeOverlayAndFinish();
                },
                2500);
    }

    private void finishNow() {
        if (finished) {
            return;
        }

        finished = true;
        mainHandler.removeCallbacksAndMessages(null);
        overridePendingTransition(0, 0);
        finishAndRemoveTask();
    }

    private void removeOverlayAndFinish() {
        if (finished) {
            return;
        }

        finished = true;
        mainHandler.removeCallbacksAndMessages(null);

        if (windowManager != null && overlayContainer != null) {
            try {
                windowManager.removeViewImmediate(overlayContainer);
            } catch (RuntimeException exception) {
                Log.w(
                        TAG,
                        "overlay removal failed",
                        exception);
            }

            overlayContainer = null;
        }

        overridePendingTransition(0, 0);
        finishAndRemoveTask();
    }

    @Override
    protected void onDestroy() {
        Log.i(
                TAG,
                "activity destroyed finished=" + finished);
        super.onDestroy();
    }
}
