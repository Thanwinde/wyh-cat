package com.wyhCat.engin;

import jakarta.servlet.*;

import java.io.IOException;

public class FilterChainImpl implements FilterChain {
    final Filter[] filters;
    final Servlet servlet;
    final int total; // Filter总数量
    int index = 0; // 下一个要处理的Filter[index]

    public FilterChainImpl(Filter[] filters, Servlet servlet) {
        this.filters = filters;
        this.servlet = servlet;
        this.total = filters.length;
    }

    @Override
    //TODO 实现order
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (index < total) {
            int current = index;
            index++;
            // 调用下一个Filter处理:
            filters[current].doFilter(request, response, this);
        } else {
            // 调用Servlet处理:
            servlet.service(request, response);
        }
    }
}
