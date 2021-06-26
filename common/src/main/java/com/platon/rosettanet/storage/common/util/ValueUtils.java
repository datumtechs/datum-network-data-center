package com.platon.rosettanet.storage.common.util;

public class ValueUtils {
    public static int intValue(Integer i){
        if (i==null){
            return 0;
        }else{
            return i.intValue();
        }
    }
    public static int intValue(Long l){
        if (l==null){
            return 0;
        }else{
            return Math.toIntExact(l.longValue());
        }
    }

    public static long longValue(Long l){
        if (l==null){
            return 0L;
        }else{
            return l.longValue();
        }
    }
}
