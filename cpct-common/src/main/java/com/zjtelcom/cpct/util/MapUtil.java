package com.zjtelcom.cpct.util;

import java.util.List;

public class MapUtil {

    public static Integer getIntNum(Object obj){
        try {
            return  Integer.parseInt(String.valueOf(obj));
        }catch (Throwable e){

        }
        return  0;
    }

    public static Long getLongNum(Object obj){
        try {
            return  Long.valueOf(String.valueOf(obj));
        }catch (Throwable e){

        }
        return  0L;
    }

    public static String getString(Object obj){
        try {
            return obj.toString();
        }catch (Throwable e){
            return  "";
        }
    }


}
