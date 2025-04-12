package com.wyhCat.engin.mapping;

import jakarta.servlet.Servlet;

/**
 * @author nsh
 * @data 2025/4/7 19:10
 * @description 用来存储一条servlet的映射url
 **/
public class ServletMapping extends AbstractMapping {

    public final Servlet servlet ;

    public ServletMapping(String urlPattern,Servlet servlet) {
        super(urlPattern);
        this.servlet = servlet;
    }

}
