package vn.kyoz.godot.notch;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

public class Notch extends GodotPlugin {
    private static final String TAG = "GodotNotch";
    private Activity activity;
    private Context context;


    public Notch(Godot godot) {
        super(godot);
        activity = getActivity();
        context = activity.getApplicationContext();

    }

    @NonNull
    @Override
    public String getPluginName() {
        return getClass().getSimpleName();
    }

    @UsedByGodot
    public int get_notch_height() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    return cutout.getSafeInsetTop();
                }
            }
        }
        return 0;  // No notch or below API level P
    }

    @UsedByGodot
    public int get_bottom_safe_inset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    return cutout.getSafeInsetBottom();
                }
            }
        }
        return 0;  // No bottom inset or below API level P
    }
}
