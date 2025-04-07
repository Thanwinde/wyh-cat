package com.wyhCat.utils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

/**
 * @author nsh
 * @data 2025/4/7 20:18
 * @description 处理注释的工具类
 **/

public class AnnoUtils {

    public static String getServletName(Class<? extends Servlet> clazz) {
        //通过获取@WebServlet上的name值（如果有的话）
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w != null && !w.name().isEmpty()) {
            return w.name();
        }
        //该属性用于给 Servlet 指定一个逻辑名称，便于在配置中引用或者在管理控制台中辨识。如果未指定，默认名称通常会采用类的简单名称
        //没有该属性就会返回默认名（用类名命名）
        return defaultNameByClass(clazz);
    }

    public static String getFilterName(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        //获取过滤器名
        if (w != null && !w.filterName().isEmpty()) {
            return w.filterName();
        }
        return defaultNameByClass(clazz);
    }

    public static Map<String, String> getServletInitParams(Class<? extends Servlet> clazz) {
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w == null) {
            return Map.of();
        }
        // 将注解中定义的初始化参数数组转换为 Map 返回
        //该属性用于给 Servlet 提供初始化参数。通过配置 initParams，可以在 Servlet 初始化时将一些配置信息传入。
        // 例如，可以指定配置文件路径、数据库连接信息等。它是一个 WebInitParam 数组，每个 WebInitParam 包含 name 和 value 两个属性。
        return initParamsToMap(w.initParams());
    }

    public static Map<String, String> getFilterInitParams(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return Map.of();
        }
        // 将注解中定义的初始化参数数组转换为 Map 返回，同上
        return initParamsToMap(w.initParams());
    }

    public static String[] getServletUrlPatterns(Class<? extends Servlet> clazz) {
        //value 和 urlPatters 用来指定Url映射，告诉容器哪些请求由这个处理
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w == null) {
            return new String[0];
        }
        // 合并注解中的 value() 和 urlPatterns() 两个数组（通常表示 URL 映射规则），并转换为 Set 去重后再转为数组返回
        return arraysToSet(w.value(), w.urlPatterns()).toArray(String[]::new);
    }

    public static String[] getFilterUrlPatterns(Class<? extends Filter> clazz) {
        //value 和 urlPatters 用来指定Url映射，告诉容器哪些请求由这个处理
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return new String[0];
        }
        // 合并注解中的 value() 和 urlPatterns() 两个数组，并去重后转换为数组返回
        return arraysToSet(w.value(), w.urlPatterns()).toArray(String[]::new);
    }

    public static EnumSet<DispatcherType> getFilterDispatcherTypes(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return EnumSet.of(DispatcherType.REQUEST);
        }
        // 将注解中定义的 dispatcherTypes 数组转换为 List，然后复制为 EnumSet 返回
        return EnumSet.copyOf(Arrays.asList(w.dispatcherTypes()));
    }

    private static String defaultNameByClass(Class<?> clazz) {
        // 获取类的简单名称
        String name = clazz.getSimpleName();
        // 将首字母转换为小写，并拼接后面的部分
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    private static Map<String, String> initParamsToMap(WebInitParam[] params) {
        //把initParams数组转换成map方便读取
        return Arrays.stream(params).collect(Collectors.toMap(p -> p.name(), p -> p.value()));
    }

    private static Set<String> arraysToSet(String[] arr1) {
        //把数组转化成集合
        Set<String> set = new LinkedHashSet<>();
        for (String s : arr1) {
            set.add(s);
        }
        return set;
    }

    private static Set<String> arraysToSet(String[] arr1, String[] arr2) {
        //把两个数组转化成集合
        Set<String> set = arraysToSet(arr1);
        set.addAll(arraysToSet(arr2));
        return set;
    }
}