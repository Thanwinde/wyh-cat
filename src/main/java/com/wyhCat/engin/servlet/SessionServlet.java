package com.wyhCat.engin.servlet;

import jakarta.servlet.Servlet;
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
@WebServlet(urlPatterns = "/session")
public class SessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        String name = req.getParameter("name");
        session.setAttribute("name", name);
        String name1 = (String) session.getAttribute("name");

        Cookie[] cookies = req.getCookies();

        String StrCookies = "";

        resp.addCookie(new Cookie("name", name));

        String html = "<h1> SessionAttribute: " + name1 + "</h1> <br />";
        for(Cookie cookie : cookies) {
            StrCookies = cookie.getName() + "=" + cookie.getValue();
            html += "<h2> reqCookies " + (StrCookies) + "</h2>  <br />";
        }

        PrintWriter writer = resp.getWriter();
        writer.println(html);
        writer.close();
    }
}
