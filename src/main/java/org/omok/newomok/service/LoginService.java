package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.repository.userDAO;

@Log4j2
public enum LoginService {
    INSTANCE;
    private userDAO dao;

    LoginService(){
        dao = userDAO.INSTANCE;
    }

    //로그인 결과로 vo를 return 하도록 한다.
    public userDAO login(String userId, String userPw){
        return dao.getUserByIdAndPassword(userId, userPw);
    }

}
