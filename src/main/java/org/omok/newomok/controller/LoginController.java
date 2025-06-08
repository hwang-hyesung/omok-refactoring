package org.omok.newomok.controller;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.service.LoginService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Log4j2
@WebServlet(displayName = "loginController", urlPatterns = "/login")
public class LoginController extends HttpServlet {
    private final LoginService service = LoginService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //바로 jsp로 전송
        req.getRequestDispatcher("/WEB-INF/view/login/login.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");
        String userPw = req.getParameter("userPw");

        UserVO user = service.login(userId, userPw);

        //로그인 실패시 세션에 메세지 저장
        if (user == null) {
            HttpSession session = req.getSession();
            session.setAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            resp.sendRedirect( "/login?error=1");
            return;
        }

        log.info("Session에 추가할 user 객체 : {}", user.toString());

        HttpSession session = req.getSession();
        session.setAttribute("loginInfo", user);

        log.info("session에서 가져온 객체 : {}", session.getAttribute("loginInfo").toString());
        resp.sendRedirect("/omok/main");
    }
}
