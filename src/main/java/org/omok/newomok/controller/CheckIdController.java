package org.omok.newomok.controller;

import org.omok.newomok.service.CheckIdService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(displayName = "checkIdController", urlPatterns = "/check-id")
public class CheckIdController extends HttpServlet {
    private final CheckIdService service = CheckIdService.INSTANCE; //싱글톤으로 서비스 객체를 가져온다.

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");

        boolean exist = service.isExistId(userId);

        // JSON으로 보내야 하므로
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // JSON 형태로 응답 보내기
        PrintWriter out = resp.getWriter();
        out.print("{\"exists\": " + exist + "}");
        out.flush();
    }
}
