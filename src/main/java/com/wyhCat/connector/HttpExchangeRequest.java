package com.wyhCat.connector;

import java.net.URI;

public interface HttpExchangeRequest  {
    //这里是转化成 HttpServletRequest 的所需要暴露信息的给出接口
    String getRequestMethod();
    URI getRequestURI();
}