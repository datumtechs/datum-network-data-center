package com.platon.datum.storage.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * @Author liushuyu
 * @Date 2022/4/26 14:58
 * @Version
 * @Desc
 */
public abstract class BaseService {


    protected String list2string(List<String> list){
        return JSONUtil.toJsonStr(list);
    }

    protected List<String> string2list(String str){
        JSONArray objects = JSONUtil.parseArray(str);
        return objects.toList(String.class);
    }
}
