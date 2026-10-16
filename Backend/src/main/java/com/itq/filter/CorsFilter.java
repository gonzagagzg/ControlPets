package com.itq.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/api/*")
public class CorsFilter implements Filter {

    private static String ORIGEN_PERMITIDO;

    static {
        String valor = System.getenv("CORS_ORIGIN");
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty("CORS_ORIGIN");
        }
        ORIGEN_PERMITIDO = (valor == null || valor.isBlank()) ? "*" : valor.trim();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        res.setHeader("Access-Control-Allow-Origin", ORIGEN_PERMITIDO);
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(204);
            return;
        }
        chain.doFilter(request, response);
    }
}
