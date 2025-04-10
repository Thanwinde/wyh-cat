package com.wyhCat.engin;

import java.util.Enumeration;

import com.wyhCat.engin.support.Attributes;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

//这是一个session的实现类，一个session对应一个
public class HttpSessionImpl implements HttpSession {

    final ServletContextImpl servletContext;
    //servlet上下文
    String sessionId;
    //session Id
    int maxInactiveInterval;
    //最大超时时间
    long creationTime;
    //创建时间
    long lastAccessedTime;
    //最后使用时间

    Attributes attributes;
    //session参数，采用线程安全的map保存

    public HttpSessionImpl(ServletContextImpl servletContext, String sessionId, int interval) {
        this.servletContext = servletContext;
        this.sessionId = sessionId;
        this.creationTime = this.lastAccessedTime = System.currentTimeMillis();
        this.attributes = new Attributes(true);
        //采用线程安全的map，因为session可能需要大量并发读写
        setMaxInactiveInterval(interval);
    }

    @Override
    public long getCreationTime() {
        return creationTime;
        //获取创建时间
    }

    @Override
    public String getId() {
        //获取id
        return this.sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        //获取
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        //获取上下文
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        //设置最大超时时间
        this.maxInactiveInterval = interval;

    }

    @Override
    public int getMaxInactiveInterval() {
        //获取最大超时时间
        return this.maxInactiveInterval;
    }

    @Override
    public void invalidate() {
        //让session失效
        checkValid();
        this.servletContext.sessionManager.remove(this);
        this.sessionId = null;
    }

    @Override
    public boolean isNew() {
        //判断session是否为最新的
        return this.creationTime == this.lastAccessedTime;
    }


    @Override
    public Object getAttribute(String name) {
        //获取对应参数
        checkValid();
        return this.attributes.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        //获取枚举类map，与老版本jdk兼容
        checkValid();
        return this.attributes.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        //设置参数
        checkValid();
        if (value == null) {
            removeAttribute(name);
        } else {
            this.attributes.setAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        //移除参数
        checkValid();
        this.attributes.removeAttribute(name);
    }

    void checkValid() {
        //检测该session是不是被移除了
        if (this.sessionId == null) {
            throw new IllegalStateException("Session is already invalidated.");
        }
    }
}