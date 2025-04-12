package com.wyhCat.engin;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nsh
 * @data 2025/4/10 14:32
 * @description
 **/
//session管理器，一个上下文只有一个
public class SessionManager {

    final Logger logger = LoggerFactory.getLogger(getClass());

    //servlet上下文
    final ServletContextImpl servletContext;

    Map<String, HttpSessionImpl> sessions = new HashMap<>();

    //默认超时时间,单位毫秒
    int inactiveInterval = 10 * 60 * 1000;

    public SessionManager(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
        logger.info("Session管理器启动");

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000L);
                } catch (InterruptedException e) {
                    break;
                }
                long now = System.currentTimeMillis();
                for(HttpSessionImpl session : sessions.values()) {
                    if(session.getLastAccessedTime() + inactiveInterval < now) {
                        logger.info("session过期: {}", session.getId());
                        session.invalidate();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public HttpSession getSession(String sessionId) {
        HttpSessionImpl session = sessions.get(sessionId);
        if (session == null) {
            session = new HttpSessionImpl(this.servletContext, sessionId, inactiveInterval);
            this.servletContext.invokeSessionCreated(session);
            sessions.put(sessionId,session);
        }else{
            session.lastAccessedTime = System.currentTimeMillis();
        }
        return session;
    }

    public void remove(HttpSession session) {
        this.servletContext.invokeSessionDestroyed(session);
        this.sessions.remove(session.getId());
    }
}
