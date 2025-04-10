package com.wyhCat.engin.servlet;

import jakarta.servlet.ServletException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author nsh
 * @data 2025/4/10 17:05
 * @description
 **/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/cookie")
public class CookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String oldCookie = req.getHeader("Cookie");
        req.getSession();
        String name = "wyh";
        String nickname = "5mm";
        resp.addCookie(new Cookie(name, nickname));
        oldCookie = req.getHeader("Cookie");
        String html = "<h1>old cookies: " + (oldCookie) + " .</h1>";
        resp.setContentType("text/html");
        PrintWriter pw = resp.getWriter();
        pw.write(html);
        pw.close();
    }
}
