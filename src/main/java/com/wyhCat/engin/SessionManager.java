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

    //默认超时时间
    int inactiveInterval = 1000;

    public SessionManager(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
        logger.info("Session管理器启动");
    }

    public HttpSession getSession(String sessionId) {
        HttpSessionImpl session = sessions.get(sessionId);
        if (session == null) {
            session = new HttpSessionImpl(this.servletContext, sessionId, inactiveInterval);
            sessions.put(sessionId,session);
        }else{
            session.lastAccessedTime = System.currentTimeMillis();
        }
        return session;
    }

    public void remove(HttpSession session) {
        this.sessions.remove(session.getId());
    }
}
