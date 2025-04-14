package com.wyhCat.engin;

import com.wyhCat.engin.support.InitParameters;
import jakarta.servlet.*;

import java.util.*;

/**
 * @author nsh
 * @data 2025/4/9 13:10
 * @description 对于filter的实现，每个filter对应一个FilterRegistrationImpl
 **/
public class FilterRegistrationImpl implements FilterRegistration.Dynamic {

    final ServletContext servletContext;
    //servlet上下文

    final String name;
    //filter名

    final Filter filter;
    //filter对象

    final InitParameters initParameters = new InitParameters(false);
    //初始参数，每个filter专属一个

    final List<String> urlPatterns = new ArrayList<>(4);
    //记录着该filter的所有映射地址

    boolean initialized = false;
    //默认未初始化

    public FilterRegistrationImpl(ServletContextImpl servletContext, String filterName, Filter filter) {
        this.servletContext = servletContext;
        this.name = filterName;
        this.filter = filter;
    }

    //提供给servlet容器的接口
    public FilterConfig getFilterConfig() {
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return FilterRegistrationImpl.this.name;
            }

            @Override
            public ServletContext getServletContext() {
                return FilterRegistrationImpl.this.servletContext;
            }

            @Override
            public String getInitParameter(String name) {
                return FilterRegistrationImpl.this.initParameters.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return FilterRegistrationImpl.this.initParameters.getInitParameterNames();
            }
        };
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> enumSet, boolean b, String... strings) {
        //不支持
        throw new UnsupportedOperationException("addMappingForServletNames");
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return List.of(name);
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        checkNotInitialized("addMappingForUrlPatterns");
        if (!dispatcherTypes.contains(DispatcherType.REQUEST) || dispatcherTypes.size() != 1) {
            //看看分派类型是不是REQUEST，filter目前只支持用户直接发起的请求
            //FORWARD 转发
            //INCLUDE 表示请求包含了另一个资源的输出内容,如jsp
            //ASYNC 表示请求处于异步（asynchronous）处理状态
            //ERROR 表示请求是在错误处理流程中转发的
            throw new IllegalArgumentException("Only support DispatcherType.REQUEST.");
        }
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException("Missing urlPatterns.");
        }
        //将对应的urlPatterns加入其中
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        //获取全部映射url
        return this.urlPatterns;
    }

    @Override
    public String getName() {
        //获取filter名
        return this.name;
    }

    @Override
    public String getClassName() {
        //获取filter类名
        return filter.getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        //设置初始化参数
        checkNotInitialized("setInitParameter");
        return this.initParameters.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String s) {
        //获取初始参数
        return this.initParameters.getInitParameter(s);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        //设置初始参数
        checkNotInitialized("setInitParameter");
        return this.initParameters.setInitParameters(initParameters);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return this.initParameters.getInitParameters();
    }

    private void checkNotInitialized(String name) {
        if (initialized) {
            throw new IllegalStateException("Filter还没有初始化:" + name);
        }
    }

    @Override
    public void setAsyncSupported(boolean b) {
        //暂不支持异步
        checkNotInitialized("setInitParameter");
        if (b) {
            throw new UnsupportedOperationException("Async is not supported.");
        }
    }
}
