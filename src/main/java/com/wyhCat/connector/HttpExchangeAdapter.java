package com.wyhCat.connector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author nsh
 * @data 2025/4/7 12:58
 * @description 作为一个转换器，把HttpExchange中可能会用到的接口抽取出来，方便servlet容器对其使用
 **/

public class HttpExchangeAdapter implements HttpExchangeRequest, HttpExchangeResponse {
    final HttpExchange exchange;

    byte[] requestBodyData;

    public HttpExchangeAdapter(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public Integer getStatusCode() {
        return (Integer) exchange.getResponseCode();
    }

    @Override
    public String getRequestMethod() {
        return this.exchange.getRequestMethod();
    }

    @Override
    public URI getRequestURI() {
        return this.exchange.getRequestURI();
    }

    @Override
    public Headers getRequestHeaders() {
        return this.exchange.getRequestHeaders();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.exchange.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.exchange.getLocalAddress();
    }

    @Override
    public byte[] getRequestBody() throws IOException {
       if(this.requestBodyData == null) {
           try(InputStream is = this.exchange.getRequestBody()) {
               this.requestBodyData = is.readAllBytes();
           }
       }
        return this.requestBodyData;
    }

    public String getProtocol(){
        return this.exchange.getProtocol();
    }

    public String getServerName(){
        return this.exchange.getLocalAddress().getAddress().getHostAddress();
    }

    @Override
    public int getServerPort() {
        return this.exchange.getLocalAddress().getPort();
    }

    @Override
    public Headers getResponseHeaders() {
        return this.exchange.getResponseHeaders();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.exchange.sendResponseHeaders(rCode, responseLength);
    }

    @Override
    public OutputStream getResponseBody() {
        return this.exchange.getResponseBody();
    }
}




