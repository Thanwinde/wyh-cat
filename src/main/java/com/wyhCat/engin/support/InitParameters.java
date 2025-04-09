package com.wyhCat.engin.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//这是存储一个组件的初始参数的类，一个类只有一个
public class InitParameters extends LazyMap<String> {

    public boolean setInitParameter(String name, String value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or empty.");
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value is null or empty.");
        }
        if (super.containsKey(name)) {
            //查询参数是否已经存在
            return false;
        }
        //加入参数
        super.put(name, value);
        return true;
    }

    public String getInitParameter(String name) {
        //获取参数
        return super.get(name);
    }


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (initParameters == null) {
            throw new IllegalArgumentException("initParameters is null.");
        }
        if (initParameters.isEmpty()) {
            return Set.of();
        }
        Set<String> conflicts = new HashSet<>();
        for (String name : initParameters.keySet()) {
            String value = initParameters.get(name);
            if (value == null) {
                throw new IllegalArgumentException("initParameters contains null value by name: " + name);
            }
            if (super.containsKey(name)) {
                conflicts.add(name);
            } else {
                super.put(name, value);
            }
        }
        //对于冲突的参数会将其返回
        //其余正常加载
        return conflicts;
    }

    public Map<String, String> getInitParameters() {
        return super.map();
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(super.map().keySet());
    }
}