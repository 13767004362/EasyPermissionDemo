package com.xingen.easypermissiondemo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ${xingen} on 2017/11/1.
 * blog: http://blog.csdn.net/hexingen
 */

public class ToastUtils {

    public static void showToast(Context context,String content){
        Toast.makeText(context.getApplicationContext(),content,Toast.LENGTH_SHORT).show();
    }
}
