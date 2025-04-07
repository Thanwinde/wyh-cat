package com.wyhCat.connector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wyhCat.engin.HttpServletRequestImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author nsh
 * @data 2025/4/7 13:22
 * @description
 **/
public class HttpConnector implements HttpHandler,AutoCloseable{

    final Logger logger = LoggerFactory.getLogger(getClass());

    final HttpServer httpServer;

    final String host;

    final int port;

    public HttpConnector(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
        this.httpServer.start();
        logger.info("wyhCat 启动于 {}:{}", host, port);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
/*        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String query = uri.getRawQuery();
        logger.info("{}: {}?{}", method, path, query);

        Headers respHeaders = exchange.getResponseHeaders();
        respHeaders.set("Content-Type", "text/html; charset=utf-8");
        respHeaders.set("Cache-Control", "no-cache");

        exchange.sendResponseHeaders(200, 0);

        String s = "<h1>Hello, world.</h1><p>" + LocalDateTime.now().withNano(0) + "</p>";

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(s.getBytes(StandardCharsets.UTF_8));
        }*/
        var adapter = new HttpExchangeAdapter(exchange);
        var request = new HttpServletRequestImpl(adapter);

    }

    public void process(HttpServletRequest request, HttpServletResponse response){

    }

    @Override
    public void close() throws Exception {
        //延迟三秒关闭HTTP实例
        httpServer.stop(3);
    }
}
