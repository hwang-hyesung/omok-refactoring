package org.omok.newomok.repository;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.StatVO;
import org.omok.newomok.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public enum MainDAO {
    INSTANCE;

    /* 상위 10명 불러오는 함수 - 승률순, 전체 게임 판순, 유저 아이디 순 */
    public List<StatVO> getRanks() {
        List<StatVO> result = new ArrayList<>();
        String sql = "SELECT U.USER_ID, S.WIN, S.LOSE, S.RATE, U.IMAGE\n" +
                "FROM USER U\n" +
                "JOIN STAT S ON U.USER_ID = S.USER_ID\n" +
                "ORDER BY S.RATE DESC, (S.WIN + S.LOSE) DESC, U.USER_ID\n" +
                "LIMIT 10;\n";

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            @Cleanup ResultSet rs = pstmt.executeQuery();

            int rankCnt = 1;
            while(rs.next()) {
                String userId = rs.getString("USER_ID");
                int win = rs.getInt("WIN");
                int lose = rs.getInt("LOSE");
                double rate = rs.getDouble("RATE");
                int imageNum = rs.getInt("IMAGE");
                StatVO vo = new StatVO(rankCnt++, userId, win, lose, rate, imageNum);
                result.add(vo);
            }
        } catch(Exception e) {
            log.error("erros-getRanks: {}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /* 내 랭킹 불러오는 함수 */
    public int getMyRank(String userId) {
        String sql = "SELECT USER_ID, RANK\n" +
                "FROM (\n" +
                "SELECT \n" +
                "U.USER_ID,\n" +
                "ROW_NUMBER() OVER (\n" +
                "ORDER BY S.RATE DESC, (S.WIN + S.LOSE) DESC, U.USER_ID\n" +
                ") AS RANK\n" +
                "FROM USER U\n" +
                "JOIN STAT S ON U.USER_ID = S.USER_ID\n" +
                ") A\n" +
                "WHERE A.USER_ID = ?";
        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            @Cleanup ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getInt("RANK");
            }
        } catch(Exception e) {
            log.error("erros-getMyRank: {}", e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
