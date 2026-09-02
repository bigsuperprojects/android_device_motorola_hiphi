package org.lineageos.performanceprofile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

public class OverlayService extends Service {

    private WindowManager mWindowManager;
    private ImageView mOverlayView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int currentProfile = intent.getIntExtra("current_profile", 1);
            int nextProfile = intent.getIntExtra("next_profile", 1);
            Rect iconBounds = intent.getParcelableExtra("bounds");

            if (iconBounds != null) {
                mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                mOverlayView = new ImageView(this);

                int animResId;
                if (currentProfile == 0 && nextProfile == 1) animResId = R.drawable.sweep_low_med;
                else if (currentProfile == 1 && nextProfile == 2) animResId = R.drawable.sweep_med_high;
                else animResId = R.drawable.sweep_high_low;

                AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getDrawable(animResId);
                mOverlayView.setImageDrawable(avd);

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    iconBounds.width(),
                    iconBounds.height(),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
                );

                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = iconBounds.left;
                params.y = iconBounds.top;

                try {
                    mWindowManager.addView(mOverlayView, params);
                    if (avd != null) {
                        avd.start();
                    }
                } catch (Exception e) {
                    mWindowManager = null;
                }
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mWindowManager != null && mOverlayView != null) {
                try {
                    mWindowManager.removeView(mOverlayView);
                } catch (Exception ignored) {}
            }
            stopSelf();
        }, 450);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
