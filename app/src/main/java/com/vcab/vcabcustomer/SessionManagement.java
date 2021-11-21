package com.vcab.vcabcustomer;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SessionManagement {


    public void setFBToken(Context con, String fbToken) {
        SharedPreferences.Editor editor = con.getSharedPreferences("userDetails", MODE_PRIVATE).edit();
        editor.putString("fbToken", fbToken);
        editor.apply();
    }

    public String getFBToken(Context con) {
        SharedPreferences prefs = con.getSharedPreferences("userDetails", MODE_PRIVATE);
        return prefs.getString("fbToken", "token name defined");
    }

    public static boolean isConnectedToInternet(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            if (netInfo != null) {

                for (int i = 0; i < netInfo.length; i++) {

                    if (netInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;

                  /*  if (SessionManagement.isConnectedToInternet(context)) {

                      } else {
                         Snackbar.make(view, "Check your connectivity!", Snackbar.LENGTH_SHORT).show();
                         Toast.makeText(this, "Check your connectivity!", Toast.LENGTH_SHORT).show();

                      }
                    */
                }
            }
        }
        return false;
    }


}
