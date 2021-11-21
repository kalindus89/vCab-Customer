package com.vcab.vcabcustomer;

import android.content.Context;
import android.widget.Toast;

public class MessagesClass {


    public static void showToastMsg(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
