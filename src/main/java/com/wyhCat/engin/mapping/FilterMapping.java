package com.wyhCat.engin.mapping;

import jakarta.servlet.Filter;

import java.util.regex.Pattern;

/**
 * @author nsh
 * @data 2025/4/9 12:56
 * @description
 **/
public class FilterMapping extends AbstractMapping {

    public final Filter filter;

    public FilterMapping(String urlPattern,Filter filter) {
        super(urlPattern);
        this.filter = filter;
    }
}
