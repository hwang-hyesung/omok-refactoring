package org.omok.newomok.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.GameDAO;
import org.omok.newomok.repository.MatchDAO;
import org.omok.newomok.repository.UserDAO;

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

@Log4j2
@WebServlet(displayName = "gameController", urlPatterns = "/omok/play")
public class GameController extends HttpServlet {
    private final MatchDAO matchDAO = MatchDAO.INSTANCE;
    private final GameDAO gameDAO = GameDAO.INSTANCE;
    private final UserDAO userDAO = UserDAO.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String gameId = req.getParameter("gameId");
        req.setAttribute("gameId", gameId);
        req.getRequestDispatcher("/WEB-INF/view/game/game.jsp").forward(req, resp); // JSP로 포워딩
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // JSON body 읽기
        BufferedReader reader = req.getReader();
        String jsonBody = reader.lines().collect(Collectors.joining());
        log.info("jsonBody : {}",jsonBody);

        JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();

        int gameId = json.get("gameId").getAsInt();
        String winnerId = json.get("winnerId").getAsString();

        log.info("gameId : {}",gameId);
        log.info("winnerId : {}",winnerId);

        GameVO gameVO = matchDAO.getGameById(gameId);
        gameVO.setWinnerId(winnerId);

        HttpSession session = req.getSession();
        UserVO userVO = (UserVO) session.getAttribute("loginInfo");
        userVO = userDAO.getUserById(userVO.getUserId());

        // 게임 상태 FINISHED, 승자 업데이트용 VO 생성
        GameVO finishedGame = GameVO.builder()
                .gameId(gameId)
                .status(GameVO.GameStatus.FINISHED)
                .winnerId(winnerId)
                .build();

        int updatedCount = gameDAO.finishGame(finishedGame);
        log.info("updatedCount : {}",updatedCount);

        JsonObject responseJson = new JsonObject();
        if (updatedCount > 0) {
            responseJson.addProperty("result", "success");

            // 현재 로그인한 유저 기준으로 승리 여부 판단
            boolean isWinner = winnerId.equals(userVO.getUserId());

            int win = userVO.getWin();
            int lose = userVO.getLose();
            int rate;

            if (isWinner) {
                win++;
            } else {
                lose++;
            }

            int totalGames = win + lose;
            rate = totalGames == 0 ? 0 : (int) ((win / (double) totalGames) * 100);
            userDAO.updateStat(userVO.getUserId(), win, lose, rate);

            // 세션 정보 갱신
            userVO.setWin(win);
            userVO.setLose(lose);
            userVO.setRate(rate);
            session.setAttribute("loginInfo", userVO);
            responseJson.addProperty("userId", userVO.getUserId());
            responseJson.addProperty("image", userVO.getImage());
            responseJson.addProperty("win", userVO.getWin());
            responseJson.addProperty("lose", userVO.getLose());
            responseJson.addProperty("isWinner", isWinner);


            log.info("게임 종료 처리 완료 - gameId: {}, winnerId: {}", gameId, winnerId);
        } else {
            responseJson.addProperty("result", "fail");
            log.error("게임 종료 처리 실패 - gameId: {}, winnerId: {}", gameId, winnerId);
        }

        sendJson(resp, responseJson);
    }

    private void sendJson(HttpServletResponse resp, JsonObject jsonObject) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(new Gson().toJson(jsonObject));
        out.flush();
    }
}
