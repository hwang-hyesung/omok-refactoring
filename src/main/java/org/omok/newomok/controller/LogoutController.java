package org.omok.newomok.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(displayName = "logoutController", urlPatterns = "/logout")
public class LogoutController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        session.removeAttribute("loginInfo"); //세션에서 loginInfo 제거, 로그 아웃
        session.invalidate();

        resp.sendRedirect("/login"); //다시 로그인 화면으로 이동한다.
    }
}
