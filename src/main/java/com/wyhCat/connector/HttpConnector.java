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
import com.wyhCat.engin.servlet.*;
import com.wyhCat.engin.servlet.listener.MySessionAttributeListener;
import com.wyhCat.engin.servlet.listener.MySessionListener;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author nsh
 * @data 2025/4/7 13:22
 * @description 管理http示例的类，采用HttpExchange来处理以及返回请求，会把请求转化成servlet规范交给servlet服务器处理
 *
 **/
public class HttpConnector implements HttpHandler,AutoCloseable{

    final Logger logger = LoggerFactory.getLogger(getClass());

    final HttpServer httpServer;
    //引入jakarta，帮助处理复杂http请求，返回exchange便于操作

    final ServletContextImpl servletContextImpl;
    //servlet上下文

    final String host;
    //设置监听地址(localhost)

    final int port;
    //设置监听端口(8080)

    public HttpConnector(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.servletContextImpl = new ServletContextImpl();
        //创建servlet上下文,servlet服务器的核心
        this.servletContextImpl.initServlet(List.of(UploadServlet.class, HelloServlet.class, SessionGetServlet.class, SessionServlet.class, PostServlet.class));
        this.servletContextImpl.initFilters(List.of(HelloFilter.class, LogFilter.class));
        this.servletContextImpl.initListeners(List.of(MySessionAttributeListener.class, MySessionListener.class));
        //暂时手动导入servlet，filter和listener并初始化
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        //创建一个绑定了该host，port的http实例
        this.httpServer.createContext("/", this);
        //设置根路径以及handler（处理请求）
        this.httpServer.start();
        logger.info("wyhCat 启动于 {}:{}", host, port);
    }


    //handle处理进来的请求，关于TCP，解析什么的httpserver已经帮我们做了，我们只需要把请求转化成servlet规范并提供相关接口就行
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.info("{}: {}?{}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), exchange.getRequestURI().getRawQuery());
        HttpExchangeAdapter adapter = new HttpExchangeAdapter(exchange);
        //HttpExchangeAdapter继承了exchangeRequest和exchangeResponse，方便进行转换处理

        HttpServletResponseImpl response = new HttpServletResponseImpl(adapter);
        HttpServletRequestImpl request = new HttpServletRequestImpl(adapter,response,servletContextImpl);
        //这里先创建了response并传给request是因为request里面会有一些方法用来修改response（cookie，session）
        String url = request.getRequestURI();
        //返回ico（测试，后面会删掉）
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
            //把请求传给servlet容器处理
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
