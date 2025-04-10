package com.wyhCat.engin;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.sun.net.httpserver.Headers;
import com.wyhCat.connector.HttpExchangeResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseImpl implements HttpServletResponse {

    final HttpExchangeResponse exchangeResponse;

    final Headers headers;

    int status = 200;
    int bufferSize = 1024;
    Boolean callOutput = null;
    ServletOutputStream output;
    PrintWriter writer;

    String contentType;
    long contentLength = 0;
    List<Cookie> cookies = null;
    boolean committed = false;

    public HttpServletResponseImpl(HttpExchangeResponse exchangeResponse) {
        this.exchangeResponse = exchangeResponse;
        this.headers = exchangeResponse.getResponseHeaders();
        this.setContentType("text/html");
    }
    //这个接口会提供所有HttpServletResponse的接口并将其在内部用HttpExchangeRequest，为“转换器”
    @Override
    public PrintWriter getWriter() throws IOException {

        this.exchangeResponse.sendResponseHeaders(status, 0);
        committed = true;
        return new PrintWriter(this.exchangeResponse.getResponseBody(), true, StandardCharsets.UTF_8);
    }

    @Override
    public void setContentType(String type) {
        setHeader("Content-Type", type);
    }

    @Override
    public void setHeader(String name, String value) {
        this.exchangeResponse.getResponseHeaders().set(name, value);
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
           //todo
        return null;
        }

    @Override
    public void setCharacterEncoding(String charset) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setContentLength(int len) {
        this.contentLength = len;
    }

    @Override
    public void setContentLengthLong(long len) {
        this.contentLength = len;
    }

    @Override
    public void setBufferSize(int size) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {
        checkNotCommitted();
        this.status = 200;
        this.headers.clear();
    }

    @Override
    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
        checkNotCommitted();
        if (this.cookies == null) {
            this.cookies = new ArrayList<>();
        }
        this.cookies.add(cookie);
        String cookieValue = cookie.getName() + "=" + cookie.getValue() + ";";
        addHeader("Set-Cookie", cookieValue);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        PrintWriter pw = getWriter();
        pw.write(String.format("<h1>%d %s</h1>", sc, msg));
        pw.close();
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        PrintWriter pw = getWriter();
        pw.write(String.format("<h1>%d %s</h1>", sc, "Error!"));
        pw.close();
    }

    @Override
    public void sendRedirect(String location) throws IOException {

        this.exchangeResponse.getResponseHeaders().set("Location", location);
        this.exchangeResponse.sendResponseHeaders(302, 0);
    }


    @Override
    public void setDateHeader(String name, long date) {
        checkNotCommitted();
        this.headers.set(name, String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        checkNotCommitted();
        this.headers.add(name, String.valueOf(date));
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        checkNotCommitted();
        this.headers.set(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        checkNotCommitted();
        this.headers.add(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name).toString();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.headers.get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    void checkNotCommitted() {
        if (this.committed) {
            throw new IllegalStateException("Response is committed.");
        }
    }
}