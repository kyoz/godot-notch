package vn.kyoz.godot.notch;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import android.view.View;
import android.view.WindowManager;
import android.view.Display;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.graphics.Insets;
import androidx.annotation.NonNull;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.Dictionary;
import org.godotengine.godot.plugin.SignalInfo;
import androidx.collection.ArraySet;
import java.util.Set;

public class Notch extends GodotPlugin {
    private static final String TAG = "GodotNotch";
    private Activity activity;
    private Context context;

    private static final String ORIENTATION_CHANGED_SIGNAL = "screen_orientation_changed";

    private OrientationEventListener mOrientationEventListener;
    private int lastRotation = -1;

    public Notch(Godot godot) {
        super(godot);
        activity = getActivity();
        context = activity.getApplicationContext();
        Log.i(TAG, "GodotNotch Plugin Initialized Successfully.");
        startRotationListener();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Notch";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo(ORIENTATION_CHANGED_SIGNAL, String.class));
        return signals;
    }

    @Override
    public void onMainDestroy() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
            mOrientationEventListener = null;
            Log.i(TAG, "Rotation Listener stopped.");
        }
    }

    private void startRotationListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;

        mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (activity == null) return;

                Display display = windowManager.getDefaultDisplay();
                final int newRotation = display.getRotation();

                if (newRotation != Notch.this.lastRotation) {
                    final String newOrientationString = get_current_orientation_string_by_rotation(newRotation);
                    
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            emitSignal(ORIENTATION_CHANGED_SIGNAL, newOrientationString);
                        }
                    });

                    Notch.this.lastRotation = newRotation;
                }
            }
        };

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
            lastRotation = windowManager.getDefaultDisplay().getRotation();
        }
    }

    @UsedByGodot
    public String get_current_orientation_string() {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return "Unknown";
        int rotation = windowManager.getDefaultDisplay().getRotation();
        return get_current_orientation_string_by_rotation(rotation);
    }

    private String get_current_orientation_string_by_rotation(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0: return "Portrait";
            case Surface.ROTATION_90: return "Landscape";
            case Surface.ROTATION_180: return "Reverse Portrait";
            case Surface.ROTATION_270: return "Reverse Landscape";
            default: return "Unknown";
        }
    }

    @UsedByGodot
    public Dictionary get_safe_insets() {
        Dictionary safe_insets = new Dictionary();
        safe_insets.put("top", 0);
        safe_insets.put("bottom", 0);
        safe_insets.put("left", 0);
        safe_insets.put("right", 0);

        if (activity == null) return safe_insets;

        View decorView = activity.getWindow().getDecorView();
        WindowInsets insets = decorView.getRootWindowInsets();
        if (insets == null) return safe_insets;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Insets cutoutInsets = insets.getInsets(WindowInsets.Type.displayCutout());
            safe_insets.put("top", cutoutInsets.top);
            safe_insets.put("bottom", cutoutInsets.bottom);
            safe_insets.put("left", cutoutInsets.left);
            safe_insets.put("right", cutoutInsets.right);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                safe_insets.put("top", cutout.getSafeInsetTop());
                safe_insets.put("bottom", cutout.getSafeInsetBottom());
                safe_insets.put("left", cutout.getSafeInsetLeft());
                safe_insets.put("right", cutout.getSafeInsetRight());
            }
        }
        return safe_insets;
    }
}
