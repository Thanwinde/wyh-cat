package com.wyhCat.engin.servlet.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nsh
 * @data 2025/4/12 14:02
 * @description
 **/
@WebListener
public class MySessionAttributeListener implements HttpSessionAttributeListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        logger.info(">>> 添加Session attribute: {} = {}", event.getName(), event.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        logger.info(">>> 移除Session attribute: {} = {}", event.getName(), event.getValue());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        logger.info(">>> 替换Session attribute为: {} = {}", event.getName(), event.getValue());
    }
}
