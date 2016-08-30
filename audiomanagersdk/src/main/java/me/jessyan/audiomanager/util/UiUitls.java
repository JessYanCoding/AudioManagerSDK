package me.jessyan.audiomanager.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by jess on 7/26/16.
 */
public class UiUitls {
   public static Toast mToast;
    /**
     * 单列toast
     *
     * @param string
     */

    public static void makeText(Context context, String string) {
        if (mToast == null) {
            mToast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        }
        mToast.setText(string);
        mToast.show();
    }
}
