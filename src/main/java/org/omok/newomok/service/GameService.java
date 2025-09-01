package org.omok.newomok.service;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.repository.UserDAO;
import org.omok.newomok.repository.GameDAO;
import org.omok.newomok.repository.MatchDAO;

@Log4j2
public class GameService {
    private static GameService instance;
    private final MatchDAO matchDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    private GameService() {
        this.matchDAO = MatchDAO.INSTANCE;
        this.gameDAO = GameDAO.INSTANCE;
        this.userDAO = UserDAO.INSTANCE;
    }

    public static synchronized GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    public GameVO createGame(String player1Id) {
        GameVO game = GameVO.builder()
            .status(GameVO.GameStatus.WAITING)
            .player1(player1Id)
            .build();
        return MatchDAO.makeGame(game);
    }

    public GameVO joinGame(int gameId, String player2Id) {
        GameVO game = MatchDAO.getGameById(gameId);
        if (game != null && game.getStatus() == GameVO.GameStatus.WAITING) {
            game.setPlayer2(player2Id);
            game.setStatus(GameVO.GameStatus.PLAYING);
            matchDAO.updateGame(game);
            return game;
        }
        return null;
    }

    public void finishGame(int gameId, String winnerId) {
        GameVO game = GameVO.builder()
            .gameId(gameId)
            .status(GameVO.GameStatus.FINISHED)
            .winnerId(winnerId)
            .build();
        gameDAO.finishGame(game);
    }
}