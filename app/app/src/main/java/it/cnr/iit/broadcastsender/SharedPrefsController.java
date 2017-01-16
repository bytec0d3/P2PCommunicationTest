package it.cnr.iit.broadcastsender;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mattia on 14/01/17.
 */
public class SharedPrefsController {

    private static final String PREFS_NAME = "bcastprefs";

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

}
