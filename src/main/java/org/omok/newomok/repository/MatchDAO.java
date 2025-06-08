package org.omok.newomok.repository;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Log4j2
public enum MatchDAO {
    INSTANCE;

    public int findWaitingRoom() {
        String sql = "SELECT game_id FROM GAME WHERE status = 'waiting' LIMIT 1"; // 하나만 가져오도록 한다.

        int gameId = -1; // 기본값으로 -1을 설정
        try{
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement ps = conn.prepareStatement(sql);
            @Cleanup ResultSet rs = ps.executeQuery();

            if(rs.next()){
                gameId = rs.getInt("game_id");
            }
        }catch (Exception e){
            log.error("에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }
        return gameId; //만약, waiting 방이 없다면 -1을 반환한다.
    }

    // 게임 방을 만들고 다시 객체를 return 하도록 한다.
    public static GameVO makeGame(GameVO gameVO) {
        String sql = "INSERT INTO GAME (status, player1) VALUES (?, ?)";

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, gameVO.getStatus().toString());
            pstmt.setString(2, gameVO.getPlayer1());

            int cnt = pstmt.executeUpdate();

            if (cnt > 0) {
                @Cleanup ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int gameId = rs.getInt(1); // 생성된 PK
                    return getGameById(gameId); // DB에서 다시 조회해서 리턴
                }
            }
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static GameVO getGameById(int gameId) {
        String sql = "SELECT * FROM GAME WHERE game_id = ?";
        GameVO gameVO = null;

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, gameId);
            @Cleanup ResultSet rs = pstmt.executeQuery();

            // 현재 waiting 방을 기준으로 잡은 것이므로 이렇게 저장해도 된다.
            if (rs.next()) {
                gameVO = GameVO.builder()
                        .gameId(rs.getInt("game_id"))
                        .status(GameVO.GameStatus.valueOf(rs.getString("status").toUpperCase()))
                        .player1(rs.getString("player1"))
                        .player2(rs.getString("player2"))
                        .winnerId(rs.getString("winner_id"))
                        .build();
            }
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }
        return gameVO; // gameId에 해당하는 게임 정보를 반환한다.
    }

    // 게임 방에 참가자, 상태를 update 하는 메서드
    public int updateGame(GameVO newGame) {
        String sql = "UPDATE GAME SET player2 = ?, status = ? WHERE game_id = ?";
        int cnt = 0; // update된 행의 수를 저장할 변수

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newGame.getPlayer2());
            pstmt.setString(2, GameVO.GameStatus.valueOf(newGame.getStatus().toString()).name()); // enum을 문자열로 변환
            pstmt.setInt(3, newGame.getGameId());

            cnt = pstmt.executeUpdate(); // update된 행의 수를 반환받는다.
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }
        return cnt; // update된 행의 수를 반환한다.
    }
}
