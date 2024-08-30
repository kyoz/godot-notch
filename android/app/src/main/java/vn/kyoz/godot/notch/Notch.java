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
import org.godotengine.godot.Dictionary;

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
    public Dictionary get_safe_insets() {
        Dictionary safe_insets = new Dictionary();
        safe_insets.put("top", 0);
        safe_insets.put("bottom", 0);
        safe_insets.put("left", 0);
        safe_insets.put("right", 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    safe_insets.put("top", cutout.getSafeInsetTop());
                    safe_insets.put("bottom", cutout.getSafeInsetBottom());
                    safe_insets.put("left", cutout.getSafeInsetLeft());
                    safe_insets.put("right", cutout.getSafeInsetRight());

                    return safe_insets;
                }
            }
        }
        return safe_insets;
    }
}
