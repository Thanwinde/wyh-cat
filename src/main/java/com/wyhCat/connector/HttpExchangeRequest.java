package com.wyhCat.connector;

import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public interface HttpExchangeRequest  {
    //这里是转化成 HttpServletRequest 的所需要暴露信息的给出接口
    String getRequestMethod();

    URI getRequestURI();

    Headers getRequestHeaders();

    InetSocketAddress getRemoteAddress();

    InetSocketAddress getLocalAddress();

    byte[] getRequestBody() throws IOException;
}