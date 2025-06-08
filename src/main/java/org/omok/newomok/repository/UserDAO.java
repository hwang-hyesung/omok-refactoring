package org.omok.newomok.repository;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Log4j2
public enum UserDAO {
    INSTANCE;

    public UserVO getUserById(String userId) {
        String sql = "SELECT *\n" +
                "FROM USER AS u\n" +
                "JOIN STAT AS s\n" +
                "ON u.user_id = s.user_id\n" +
                "WHERE u.user_id = ?";

        UserVO user = null;
        try{
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            @Cleanup ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                user = UserVO.builder()
                        .userId(rs.getString("user_id"))
                        .userPW(rs.getString("user_password"))
                        .bio(rs.getString("bio"))
                        .image(rs.getInt("image"))
                        .win(rs.getInt("win"))
                        .lose(rs.getInt("lose"))
                        .rate(rs.getInt("rate"))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public UserVO getUserByIdAndPassword(String userId, String userPw){
        String sql = "SELECT * \n" +
                "FROM USER AS u \n" +
                "JOIN STAT AS st ON u.user_id = st.user_id \n" +
                "WHERE u.user_id = ? \n" +
                "AND u.user_password = ?";

        UserVO vo = null;
        try{
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql); //sql 쿼리 날리기
            pstmt.setString(1, userId);
            pstmt.setString(2, userPw);

            @Cleanup ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                int win = rs.getInt("win");
                int lose = rs.getInt("lose");
                //static이라 인스턴스 생성하지 않아도 호출 가능
                int rate = rs.getInt("rate");

                vo = UserVO.builder()
                        .userId(rs.getString("user_id"))
                        .userPW(rs.getString("user_password"))
                        .bio(rs.getString("bio"))
                        .image(rs.getInt("image"))
                        .win(win)
                        .lose(lose)
                        .rate(rate)
                        .build();
            }
        }catch (Exception e){
            log.error("에러 발생 : {}",e.getMessage());
            e.printStackTrace();
        }
        return vo;
    }

    public boolean isExistUserById(String userId){
        String sql = "SELECT * FROM USER WHERE user_id = ?";
        boolean exists = false;

        try{
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql); //sql 쿼리 날리기
            pstmt.setString(1, userId);

            @Cleanup ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                exists = true;
            }
        }catch (Exception e){
            log.error("에러 발생 : {}",e.getMessage());
            e.printStackTrace();
        }
        return exists;
    }

    //회원가입을 위한 insert 필요
    public int insertUser(UserVO vo) throws Exception{
        String user_sql = "INSERT INTO USER(user_id,user_password,bio,image) VALUES(?,?,?,?)";
        String stat_sql = "INSERT INTO STAT(user_id,win,lose,rate) VALUES(?,?,?,?)";

        int cnt = 0;
        Connection conn = null;
        PreparedStatement user_pstmt = null;
        PreparedStatement stat_pstmt = null;
        try{
            conn = ConnectionUtil.INSTANCE.getConnection();
            conn.setAutoCommit(false); // 수동 트랜잭션 시작

            //User 정보 업데이트
            user_pstmt = conn.prepareStatement(user_sql);

            user_pstmt.setString(1, vo.getUserId());
            user_pstmt.setString(2, vo.getUserPW());
            user_pstmt.setString(3, vo.getBio());
            user_pstmt.setInt(4, vo.getImage());

            cnt += user_pstmt.executeUpdate();

            //Stat update
            stat_pstmt = conn.prepareStatement(stat_sql);

            stat_pstmt.setString(1, vo.getUserId());
            stat_pstmt.setInt(2, vo.getWin());
            stat_pstmt.setInt(3, vo.getLose());
            stat_pstmt.setInt(4, vo.getRate());

            cnt += stat_pstmt.executeUpdate(); //업데이트가 총 두번 되었는지 확인하기 위함이다.
            conn.commit(); //다 완료 된 다음에 커밋
        } catch (Exception e){
            if(conn != null){ //에러 났는데 null이 아닌 경우 롤백
                conn.rollback();
            }
            throw new RuntimeException("회원가입 중 오류 발생", e);
        } finally {
            //여기서 수동으로 close 해준다.
            if(user_pstmt != null){
                user_pstmt.close();
            }
            if(stat_pstmt != null){
                stat_pstmt.close();
            }
            if(conn != null){
                //히카리cp를 사용중이므로, 다시 오토 커밋 키고 돌려준다.
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return cnt; //2면 업데이트 완료
    }

    public void updateStat(String userId, int win, int lose, int rate) {
        String sql = "UPDATE STAT SET win = ?, lose = ?, rate = ? WHERE user_id = ?";

        try {
            @Cleanup Connection conn = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, win);
            pstmt.setInt(2, lose);
            pstmt.setInt(3, rate);
            pstmt.setString(4, userId);

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
