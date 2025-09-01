package org.omok.newomok.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.GameDAO;
import org.omok.newomok.repository.MatchDAO;
import org.omok.newomok.repository.UserDAO;
import org.omok.newomok.util.JsonBuilderUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;


@WebServlet(displayName = "matchController", urlPatterns = "/omok/match")
public class MatchController extends HttpServlet {
    private final MatchDAO matchDAO = MatchDAO.INSTANCE;
    private final UserDAO userDAO = UserDAO.INSTANCE;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        //세션에서 userId를 가져온다.
        UserVO user = (UserVO) session.getAttribute("loginInfo");

        //2. 게임 룸 확인
        int gameId = matchDAO.findWaitingRoom();

        if(gameId == -1) {
            //2-1. 입장 가능한 룸이 없는 경우: 새로운 룸 생성
            GameVO game = new GameVO();
            game.setPlayer1(user.getUserId());
            game.setStatus(GameVO.GameStatus.WAITING);

            GameVO newGame = MatchDAO.makeGame(game); //생성한 룸의 정보

            JsonObject gameJson = JsonBuilderUtil.getGameInfo(newGame);

            //정보 발송
            sendMatchingResponse(resp, gameJson);
        } else {
            //2-2. 입장 가능한 룸이 있는 경우: 룸에 추가 후 매칭 상태로 업데이트
            GameVO game = matchDAO.getGameById(gameId);
            game.setPlayer2(user.getUserId());
            game.setStatus(GameVO.GameStatus.PLAYING);

            matchDAO.updateGame(game);

            JsonObject gameJson = JsonBuilderUtil.getGameInfo(game);

            //정보 발송
            sendMatchingResponse(resp, gameJson);
        }
    }

    private void sendMatchingResponse(HttpServletResponse resp, JsonObject gameJson) throws IOException {
        JsonObject responseJson = new JsonObject();
        responseJson.add("game", gameJson);

        Gson gson = new Gson();
        resp.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(responseJson));
            out.flush();
        }
    }
}
