package org.omok.newomok.filter;

import lombok.extern.log4j.Log4j2;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Log4j2
@WebFilter(urlPatterns = {"/*"})
public class UTF8Filter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("UTF-8 Filter");

        HttpServletRequest req = (HttpServletRequest) request;
        req.setCharacterEncoding("UTF-8");
        //request를 얻어서 utf-8로 인코딩을 해준다.

        chain.doFilter(request,response);
    }
}
