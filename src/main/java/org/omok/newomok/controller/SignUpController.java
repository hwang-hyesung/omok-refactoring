package org.omok.newomok.controller;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.service.SignUpService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
@WebServlet(displayName = "signUpController", urlPatterns = "/sign-up")
public class SignUpController extends HttpServlet {
    private final SignUpService service = SignUpService.INSTANCE; //싱글톤으로 서비스 객체를 가져온다.

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/view/signup/signup.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId  = req.getParameter("userId");
        String userPw = req.getParameter("password");
        String bio = req.getParameter("bio");
        int profileNumber = Integer.parseInt(req.getParameter("profileNumber"));

        UserVO vo = UserVO.builder()
                .userId(userId)
                .userPW(userPw)
                .bio(bio)
                .image(profileNumber)
                .build();

        int cnt = service.signUp(vo);

        if(cnt == 2){ //하나 인서트, 회원가입 성공
            log.info("회원가입 성공 : {}", vo);
            resp.sendRedirect("/login"); //회원가입 성공시 로그인 페이지로 리다이렉트
        }else{
            //회원가입 실패시
            //이거에 대한 대응은 아직 구현 안함....이건 필요하면 구현하기
            //여기 attribute로 에러 메세지를 담아서 전달한다.
            req.setAttribute("error", "회원가입에 실패했습니다.");
            resp.sendRedirect("/sign-up?error=1"); //error 파라미터를 넘긴다.
        }
    }
}
