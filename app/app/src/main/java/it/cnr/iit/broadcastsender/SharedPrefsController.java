package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;

/**
 * Created by mattia on 14/01/17.
 */
public class SharedPrefsController {

    private static final String PREFS_NAME = "bcastprefs";

    static final String PREF_UNICAST_ADDR = "unicastAddrs";
    static final String PREF_SENDING = "sending";

    public static void setMode(Context context, String mode){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(BgService.INTENT_MODE_EXTRA, mode);
        editor.apply();
    }

    public static String getMode(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(BgService.INTENT_MODE_EXTRA, BgService.MODE_STOP);
    }

    public static void setUnicastAddresses(Context context, Set<String> addresses){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putStringSet(PREF_UNICAST_ADDR, addresses);
        editor.apply();
    }

    public static Set<String> getUnicastAddresses(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(PREF_UNICAST_ADDR, null);
    }

    public static void setSending(Context context, boolean sending){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(PREF_SENDING, sending);
        editor.apply();
    }

    public static boolean getSending(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_SENDING, false);
    }

}
