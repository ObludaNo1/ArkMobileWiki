package ark.mobile.wiki.util.output;

import android.util.Log;

public class Out {

    public static String tag = "ArkWiki";

    public static void println(){ Log.w(tag, " "); }

    public static void println(Object o){ Log.w(tag, (o == null)?("null"):(o.toString())); }

    public static void println(String s){ Log.w(tag, s); }

    public static void println(int i){ Log.w(tag, Integer.toString(i)); }

    public static void println(float f){ Log.w(tag, Float.toString(f)); }

    public static void println(double d){ Log.w(tag, Double.toString(d)); }

    public static void println(char c){ Log.w(tag, Character.toString(c)); }

    public static void println(boolean b){ Log.w(tag, Boolean.toString(b)); }

    public static void println(Object type, Object data){ Log.w(type.getClass().getName(), (data == null)?("null"):(data.toString())); }

    public static void println(Object type, String s){ Log.w(type.getClass().getName(), s); }

    public static void println(Object type, int i){ Log.w(type.getClass().getName(), Integer.toString(i)); }

    public static void println(Object type, float f){ Log.w(type.getClass().getName(), Float.toString(f)); }

    public static void println(Object type, double d){ Log.w(type.getClass().getName(), Double.toString(d)); }

    public static void println(Object type, char c){ Log.w(type.getClass().getName(), Character.toString(c)); }

    public static void println(Object type, boolean b){ Log.w(type.getClass().getName(), Boolean.toString(b)); }

}
