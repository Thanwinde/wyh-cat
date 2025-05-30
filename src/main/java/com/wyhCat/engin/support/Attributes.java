package com.wyhCat.engin.support;

import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

/**
*   @author nsh
*   @data 2025/4/10 15:06
*   @description 继承了LazyMap的Attributes类，用来懒加载的管理属性，适用于session，cookie等
**/public class Attributes extends LazyMap<Object>{
    public Attributes(boolean concurrent) {
        super(concurrent);
        //concurrent为真就会转而采用线程安全的map
    }

    public Attributes() {
        this(false);
    }

    public Object getAttribute(String name) {
        Objects.requireNonNull(name, "name is null.");
        return super.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return super.keyEnumeration();
    }

    public Object setAttribute(String name, Object value) {
        Objects.requireNonNull(name, "name is null.");
        return super.put(name, value);
    }

    public Object removeAttribute(String name) {
        Objects.requireNonNull(name, "name is null.");
        return super.remove(name);
    }

    public Map<String, Object> getAttributes() {
        return super.map();
    }
}
