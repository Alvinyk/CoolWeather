package com.coolweather.android.util;

import android.util.Log;

/**
 * Created by alvin on 2017/9/1.
 */
public class LogUtil {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARE = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static int level = VERBOSE;

    private static final String _tag = "tag_weather";
    public static void v(String tag,String msg){
        if(level <= VERBOSE){
            Log.v(tag,msg);
        }
    }

    public static void d(String tag,String msg){
        if(level <= DEBUG){
            Log.d(tag,msg);
        }
    }

    public static void i(String tag,String msg){
        if(level <= INFO){
            Log.i(tag,msg);
        }
    }

    public static void w(String tag,String msg){
        if(level <= WARE){
            Log.w(tag,msg);
        }
    }

    public static void e(String tag,String msg){
        if(level <= ERROR){
            Log.e(tag,msg);
        }
    }



    public static void v(String msg){
        if(level <= VERBOSE){
            Log.v(_tag,msg);
        }
    }

    public static void d(String msg){
        if(level <= DEBUG){
            Log.d(_tag,msg);
        }
    }

    public static void i(String msg){
        if(level <= INFO){
            Log.i(_tag,msg);
        }
    }

    public static void w(String msg){
        if(level <= WARE){
            Log.w(_tag,msg);
        }
    }

    public static void e(String msg){
        if(level <= ERROR){
            Log.e(_tag,msg);
        }
    }
}
