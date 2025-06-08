package org.omok.newomok.repository;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Log4j2
public enum GameDAO {
    INSTANCE;

    // 게임이 끝났을때 상태 바꾸는 함수
    public int finishGame(GameVO finishedGame) {
        String sql = "UPDATE GAME SET status = ?, winner_id = ? WHERE game_id = ?";
        int cnt = 0; // update된 행의 수를 저장할 변수

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, GameVO.GameStatus.FINISHED.name());
            pstmt.setString(2, finishedGame.getWinnerId());
            pstmt.setInt(3, finishedGame.getGameId());

            cnt = pstmt.executeUpdate(); // update된 행의 수를 반환받는다.
        } catch (Exception e) {
            log.error("finishGame 에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }
        return cnt; // update된 행의 수를 반환한다.
    }
}
