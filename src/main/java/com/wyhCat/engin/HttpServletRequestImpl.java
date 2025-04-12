package com.wyhCat.engin;

import java.io.*;
import java.net.URLDecoder;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;
import com.wyhCat.connector.HttpExchangeRequest;
import com.wyhCat.engin.support.Parameters;
import com.wyhCat.utils.HttpUtils;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;


public class HttpServletRequestImpl implements HttpServletRequest {

    final ServletContextImpl servletContext;
    final HttpExchangeRequest exchangeRequest;
    final HttpServletResponse response;

    final Parameters parameters;

    Boolean inputCalled = null;

    Headers headers;

    public HttpServletRequestImpl(HttpExchangeRequest exchangeRequest,HttpServletResponse response,ServletContextImpl servletContext) {
        this.servletContext = servletContext;
        this.exchangeRequest = exchangeRequest;
        this.headers = this.exchangeRequest.getRequestHeaders();
        this.response = response;

        this.parameters = new Parameters(exchangeRequest, "UTF-8");
    }
    //这个接口会提供所有HttpServletRequest的接口并将其在内部用HttpExchangeRequest，为“转换器”

    @Override
    public String getMethod() {
        return exchangeRequest.getRequestMethod();
    }

    @Override
    public String getRequestURI() {
        return this.exchangeRequest.getRequestURI().getPath();
    }

    @Override
    public String getParameter(String name) {
        return this.parameters.getParameter(name);
    }

    Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        String[] ss = Pattern.compile("\\&").split(query);
        Map<String, String> map = new HashMap<>();
        for (String s : ss) {
            int n = s.indexOf('=');
            if (n >= 1) {
                String key = s.substring(0, n);
                String value = s.substring(n + 1);
                map.putIfAbsent(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    //提取出query参数

    @Override
    public Object getAttribute(String name) {
        return this.exchangeRequest.getRequestHeaders().entrySet();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.exchangeRequest.getRequestHeaders().keySet());
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
    }

    @Override
    public int getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getContentType() {
        return this.exchangeRequest.getRequestHeaders().get("Content-Type").toString();
    }

    @Override
    //获取字节输入流，读取二进制数据（如文件上传、图片、音频、视频等），不能同时再调用 getReader
    public ServletInputStream getInputStream() throws IOException {
        if(this.inputCalled == null) {
            this.inputCalled = true;
            return new ServletInputStreamImpl(this.exchangeRequest.getRequestBody());
        }
        throw new IllegalStateException("Cannot reopen input stream after " + (this.inputCalled ? "getInputStream()" : "getReader()") + " was called.");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return this.parameters.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters.getParameterMap();
    }

    @Override
    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    //通过字符流读取，适用于读取文本，不能与 getInputStream 一同使用
    @Override
    public BufferedReader getReader() throws IOException {
        if (this.inputCalled == null) {
            this.inputCalled = Boolean.FALSE;
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.exchangeRequest.getRequestBody()), StandardCharsets.UTF_8));
        }
        throw new IllegalStateException("Cannot reopen input stream after " + (this.inputCalled ? "getInputStream()" : "getReader()") + " was called.");

    }

    @Override
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeAttribute(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProtocolRequestId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        String cookiesValue = this.getHeader("Cookie");  // 获取Cookie头部值
        if (cookiesValue == null)
            return new Cookie[0];
        cookiesValue = cookiesValue.substring(1, cookiesValue.length() - 1);
        List<Cookie> cookiesList = new ArrayList<>();

        // 以分号分割 Cookie 字符串，得到每个 cookie 的内容
        String[] cookiesArray = cookiesValue.split(";");

        for (String cookiePair : cookiesArray) {
            cookiePair = cookiePair.trim();  // 去除空白字符

            // 如果 cookie 对应的内容不为空，继续处理
            if (!cookiePair.isEmpty()) {
                String[] cookieParts = cookiePair.split("=");

                if (cookieParts.length == 2) {
                    String name = cookieParts[0].trim();
                    String value = cookieParts[1].trim();

                    // 创建 Cookie 对象并添加到 cookiesList
                    Cookie cookie = new Cookie(name, value);
                    cookiesList.add(cookie);
                }
            }
        }

        // 将 cookiesList 转换为 Cookie[] 数组并返回
        return cookiesList.toArray(new Cookie[0]);
    }


    @Override
    public long getDateHeader(String name) {
        //todo
        return 0;
    }

    @Override
    public String getHeader(String name) {
        List<String> list = this.exchangeRequest.getRequestHeaders().get(name);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.toString();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(this.exchangeRequest.getRequestHeaders().get(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        HttpSession session = getSession(true);
        return session != null ? session.getId() : null;
    }

    @Override
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    //servlet要求获取session，如果cookie中存在会直接返回，否则会根据设置创建一个session返回
    public HttpSession getSession(boolean create) {
        String sessionId = null;
        Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        if(sessionId == null && !create) {
            return null;
        }
        if(sessionId == null) {
            if(this.response.isCommitted()){
                throw new IllegalStateException("Response has already been committed");
            }
            sessionId = UUID.randomUUID().toString();

            Cookie cookie = new Cookie("JSESSIONID", sessionId);
            //在响应头里面加Set-Cookie，这样浏览器会保存下来
            this.response.addCookie(cookie);
        }
        // 创建/获取 并返回
        return this.servletContext.sessionManager.getSession(sessionId);
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO Auto-generated method stub
    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }
}