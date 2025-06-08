package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.UserDAO;

@Log4j2
public enum CheckIdService {
    INSTANCE;
    private UserDAO dao;

    CheckIdService(){
        dao = UserDAO.INSTANCE;
    }

    //아이디 중복 체크를 위함
    public boolean isExistId(String userId){
        return dao.isExistUserById(userId);
    }

}
