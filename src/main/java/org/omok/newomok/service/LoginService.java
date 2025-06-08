package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.UserDAO;

@Log4j2
public enum LoginService {
    INSTANCE;
    private UserDAO dao;

    LoginService(){
        dao = UserDAO.INSTANCE;
    }

    //로그인 결과로 vo를 return 하도록 한다.
    public UserVO login(String userId, String userPw){
        return dao.getUserByIdAndPassword(userId, userPw);
    }

}
