package org.omok.newomok.controller;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.StatVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.service.MainService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Log4j2
@WebServlet(displayName = "mainController", urlPatterns = "/omok/main")
public class MainController extends HttpServlet {
    private final MainService mainService = MainService.INSTANCE;
    /**
     * home.jsp로 매핑
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        //세션에 저장된 로그인 정보 불러오기
        UserVO userVO = (UserVO) session.getAttribute("loginInfo");

        if (userVO == null) {
            response.sendRedirect("/omok/login");
            return;
        }

        //상위 랭킹 10위 정보 불러오고 request에 저장
        List<StatVO> results = mainService.getRanks();
        request.setAttribute("ranks", results);

        //내 랭킹 정보 불러오고 request에 저장
        int myRank = mainService.getMyRank(userVO.getUserId());
        request.setAttribute("myRank", myRank);

        request.getRequestDispatcher("/WEB-INF/view/main/main.jsp").forward(request, response);
    }
}
