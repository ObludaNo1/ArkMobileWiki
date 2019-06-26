package ark.mobile.wiki.util.units;

import android.content.Context;

import ark.mobile.wiki.MainActivity;

public abstract class UnitConverter {

    public static float dpFromPx(final float px) {
        return dpFromPx(MainActivity.getContext(), px);
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final float dp) {
        return pxFromDp(MainActivity.getContext(), dp);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
