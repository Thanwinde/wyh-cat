package com.wyhCat.engin.mapping;

import jakarta.servlet.Servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nsh
 * @data 2025/4/7 19:10
 * @description
 **/
public class ServletMapping extends AbstractMapping {

    public final Servlet servlet ;

    public ServletMapping(String urlPattern,Servlet servlet) {
        super(urlPattern);
        this.servlet = servlet;
    }

}
