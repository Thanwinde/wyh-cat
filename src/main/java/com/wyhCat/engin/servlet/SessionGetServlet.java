package com.wyhCat.engin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author nsh
 * @data 2025/4/11 12:35
 * @description
 **/
@WebServlet(urlPatterns = "/getsession")
public class SessionGetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Cookie[] cookies = req.getCookies();
        String name = (String) session.getAttribute("name");

        String StrCookies;

        String html = "<h1> 获得的SessionAttribute: " + name + "</h1> <br />";
        for(Cookie cookie : cookies) {
            StrCookies = cookie.getName() + "=" + cookie.getValue();
            html += "<h2> 获得的reqCookies " + (StrCookies) + "</h2>  <br />";
        }
        resp.setContentType("text/html; charset=utf-8");

        PrintWriter writer = resp.getWriter();
        writer.println(html);
        writer.close();

    }
}
