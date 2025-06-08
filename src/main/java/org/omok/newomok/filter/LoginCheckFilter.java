package org.omok.newomok.filter;

import lombok.extern.log4j.Log4j2;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Log4j2
//그래서 반드시 모든 기능의 endpoint는 */omok/* 으로 맞춰야 한다!!! 차후 변경 필요할 시 변경 예정
@WebFilter(urlPatterns = {"/omok/*"})
public class LoginCheckFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //필터 사용을 위해서 다운캐스팅
        //session, redirect등을 사용하기 위해선 다운캐스팅 해야 한다.
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //세션안에  있는지 검사한다.
        HttpSession session = req.getSession();
        if(session.getAttribute("loginInfo") != null){
            log.info("session loginInfo 객체 확인 : {}", session.getAttribute("loginInfo"));
            chain.doFilter(req, resp);
        }else{ //세션 객체가 없음. 로그인 만료
            //다시 로그인 페이지로 이동한다.
            resp.sendRedirect("/login");
        }
    }
}
