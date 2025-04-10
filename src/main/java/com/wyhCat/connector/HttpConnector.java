package com.wyhCat.connector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wyhCat.engin.HttpServletRequestImpl;
import com.wyhCat.engin.HttpServletResponseImpl;
import com.wyhCat.engin.ServletContextImpl;
import com.wyhCat.engin.filter.HelloFilter;
import com.wyhCat.engin.filter.LogFilter;
import com.wyhCat.engin.servlet.HelloServlet;
import com.wyhCat.engin.servlet.IndexServlet;
import com.wyhCat.engin.servlet.CookieServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author nsh
 * @data 2025/4/7 13:22
 * @description Http实例相关代码
 **/
public class HttpConnector implements HttpHandler,AutoCloseable{

    final Logger logger = LoggerFactory.getLogger(getClass());

    final HttpServer httpServer;
    //引入jakarta，帮助处理复杂http请求，返回exchange便于操作

    final ServletContextImpl servletContextImpl;
    //servlet上下文

    final String host;

    final int port;

    public HttpConnector(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.servletContextImpl = new ServletContextImpl();
        this.servletContextImpl.initServlet(List.of(IndexServlet.class, HelloServlet.class, CookieServlet.class));
        this.servletContextImpl.initFilters(List.of(HelloFilter.class, LogFilter.class));
        //手动导入servlet和filter并初始化
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
        this.httpServer.start();
        logger.info("wyhCat 启动于 {}:{}", host, port);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.info("{}: {}?{}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), exchange.getRequestURI().getRawQuery());
        HttpExchangeAdapter adapter = new HttpExchangeAdapter(exchange);
        //HttpExchangeAdapter继承了exchangeRequest和exchangeResponse，方便进行转换处理

        HttpServletResponseImpl response = new HttpServletResponseImpl(adapter);
        HttpServletRequestImpl request = new HttpServletRequestImpl(adapter,response,servletContextImpl);
        String url = request.getRequestURI();
        //返回ico
        if(url.equals("/favicon.ico")){
            Headers respHeaders = exchange.getResponseHeaders();
            respHeaders.add("Content-Type", "image/x-icon");
            exchange.sendResponseHeaders(200, 0);

            InputStream in = getClass().getResourceAsStream("/favicon.ico");
            byte[] data = null;
            if (in != null) {
                data = in.readAllBytes();
            }else{
                logger.error("favicon.ico 未读取到");
            }
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
                //用try包围之后结束执行之后会自动释放资源
            }
            return;
        }
        try {
            servletContextImpl.process(request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

/*    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }*/

    @Override
    public void close() throws Exception {
        //延迟三秒关闭HTTP实例
        httpServer.stop(3);
    }
}
