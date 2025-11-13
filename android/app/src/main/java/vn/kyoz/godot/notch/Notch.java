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
import android.view.WindowManager; // Import WindowManager
import android.view.Display; // Import Display
import android.hardware.SensorManager; // Import SensorManager
import android.view.OrientationEventListener; // Import OrientationEventListener
import android.graphics.Insets; // REQUIRED FOR MODERN INSETS
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

    // Declare instance variables at the class level
    private OrientationEventListener mOrientationEventListener;
    private int lastRotation = -1; // -1: uninitialized

    // --- CONSTRUCTOR ---
    public Notch(Godot godot) {
        super(godot);
        activity = getActivity();
        context = activity.getApplicationContext();
        Log.i(TAG, "GodotNotch Plugin Initialized Successfully.");

        // Start the Rotation Listener (Optimized sensor-based solution)
        startRotationListener();
    }

    // --- MANDATORY GODOT PLUGIN METHODS ---

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

    // --- CLEANUP ---

    // onMainDestroy must be public to override GodotPlugin.onMainDestroy()
    @Override
    public void onMainDestroy() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
            mOrientationEventListener = null;
            Log.i(TAG, "Rotation Listener stopped and cleaned up.");
        }
    }

    // --- ROTATION LISTENER LOGIC ---

    // Function to start the Rotation Listener (Sensor-based)
    private void startRotationListener() {
        // Disable existing listener if any
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            Log.e(TAG, "WindowManager not found. Cannot start rotation listener.");
            return;
        }

        mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (activity == null) return;

                Display display = windowManager.getDefaultDisplay();
                final int newRotation = display.getRotation();

                // Only process when the actual screen rotation changes
                if (newRotation != Notch.this.lastRotation) {
                    final String newOrientationString = get_current_orientation_string_by_rotation(newRotation);

                    Log.d(TAG, "Rotation Changed (Sensor): " + Notch.this.lastRotation + " -> " + newRotation +
                            ", Orientation: " + newOrientationString);

                    // Run on UI Thread to ensure safe Godot Signal emission
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
            // Get initial rotation state
            lastRotation = windowManager.getDefaultDisplay().getRotation();
            Log.i(TAG, "Rotation Listener enabled. Initial rotation: " + lastRotation);
        } else {
            Log.e(TAG, "Cannot detect device orientation. Rotation Listener disabled.");
        }
    }


    @UsedByGodot
    private String get_current_orientation_string() {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return "Unknown";
        // Get Rotation from Display
        int rotation = windowManager.getDefaultDisplay().getRotation();
        return get_current_orientation_string_by_rotation(rotation);
    }

    private String get_current_orientation_string_by_rotation(int rotation) {
        // Map Surface.ROTATION values (0, 90, 180, 270) to orientation names.
        switch (rotation) {
            case Surface.ROTATION_0: // 0 degrees
                return "Portrait";
            case Surface.ROTATION_90: // 90 degrees (Standard Landscape)
                return "Landscape";
            case Surface.ROTATION_180: // 180 degrees (Reverse Portrait)
                return "Reverse Portrait";
            case Surface.ROTATION_270: // 270 degrees (Reverse Landscape)
                return "Reverse Landscape";
            default:
                return "Unknown";
        }
    }

    // --- SAFE AREA INSETS (Optimized to return the Display Cutout area) ---

    @UsedByGodot
    public Dictionary get_safe_insets() {
        Dictionary safe_insets = new Dictionary();
        safe_insets.put("top", 0);
        safe_insets.put("bottom", 0);
        safe_insets.put("left", 0);
        safe_insets.put("right", 0);

        WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
        if (insets == null) {
            return safe_insets;
        }

        // Android 10 (Q) and newer: Use WindowInsets.Type.displayCutout() for the cutout area specifically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use Type.displayCutout() to reliably get the notch/punch-hole area, even in fullscreen mode.
            Insets cutoutInsets = insets.getInsets(WindowInsets.Type.displayCutout());

            safe_insets.put("top", cutoutInsets.top);
            safe_insets.put("bottom", cutoutInsets.bottom);
            safe_insets.put("left", cutoutInsets.left);
            safe_insets.put("right", cutoutInsets.right);
            return safe_insets;

        }
        // Android 9 (P): Fallback to DisplayCutout object (older API for cutouts)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                // Get the safe area from the cutout object
                safe_insets.put("top", cutout.getSafeInsetTop());
                safe_insets.put("bottom", cutout.getSafeInsetBottom());
                safe_insets.put("left", cutout.getSafeInsetLeft());
                safe_insets.put("right", cutout.getSafeInsetRight());
            }
            return safe_insets;
        }

        // Default return (e.g., API < 28)
        return safe_insets;
    }
}