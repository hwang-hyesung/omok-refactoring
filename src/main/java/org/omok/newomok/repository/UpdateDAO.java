package org.omok.newomok.repository;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Log4j2
public enum UpdateDAO {
    INSTANCE;

    /* 한줄 소개 수정 함수 */
    public boolean modifyBio(String userId, String newBio) {
        String sql = "UPDATE USER SET BIO = ? WHERE USER_ID = ?";

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(2, userId);
            pstmt.setString(1, newBio);
            pstmt.executeUpdate();

            log.info("userId:{}, bio:{}", userId, newBio);
            return true;
        } catch (Exception e) {
            log.error("erros-getMyRank: {}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
