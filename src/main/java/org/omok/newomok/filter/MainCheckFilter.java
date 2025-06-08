package org.omok.newomok.filter;

import lombok.extern.log4j.Log4j2;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
/*
로그인한 사용자가 다시 로그인이나 회원가입에 입장하면 -> main으로 보내기 위한 필터
 */
@Log4j2
@WebFilter(urlPatterns = {"/login", "/sign-up"})
public class MainCheckFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //필터 사용을 위해서 다운캐스팅
        //session, redirect등을 사용하기 위해선 다운캐스팅 해야 한다.
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //세션안에  있는지 검사한다.
        HttpSession session = req.getSession();
        //세션 안에 로그인 객체가 있다면, main으로
        if(session.getAttribute("loginInfo") != null){
            //다시 로그인 요청하면 메인으로 보낸다.
            resp.sendRedirect("/omok/main");
            return;
        }
        //로그인 안 되어 있으면 다음 필터 진행
        chain.doFilter(req, resp);
    }
}
