package com.platon.metis.storage.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @Author liushuyu
 * @Date 2022/4/28 18:12
 * @Version
 * @Desc
 */
public class LocalDateTimeUtil {


    /**
     * 将时间戳转换成UTC时间
     * @param timestamp 时间戳
     * @return
     */
    public static LocalDateTime toUTC(long timestamp){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }
}
