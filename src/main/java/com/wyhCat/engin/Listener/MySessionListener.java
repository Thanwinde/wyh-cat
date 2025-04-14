package com.wyhCat.engin.Listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nsh
 * @data 2025/4/12 14:01
 * @description
 **/
@WebListener
public class MySessionListener implements HttpSessionListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.info(">>> 添加Session: {}",se.getSession().getId() );

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        logger.info(">>> 销毁Session: {}", se.getSession().getId());

    }
}
