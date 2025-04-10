package com.wyhCat.engin.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;


@WebFilter(urlPatterns = "/hello")
public class HelloFilter implements Filter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    Set<String> names = Set.of("Bob", "Alice", "Tom", "Jerry");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String name = req.getParameter("name");
        logger.info("Check parameter name = {}", name);
        if (name != null && names.contains(name)) {
            chain.doFilter(request, response);
        } else {
            logger.warn("Access denied: name = {}", name);
            HttpServletResponse resp = (HttpServletResponse) response;
            //resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            //resp.setContentType("text/html");
            //PrintWriter writer = resp.getWriter();
            //writer.println("<h1>Access denied!!!</h1>");
            //writer.close();
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,"Access denied");
        }
    }
}