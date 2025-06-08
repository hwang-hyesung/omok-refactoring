package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.UserDAO;

@Log4j2
public enum SignUpService {
    INSTANCE;
    private UserDAO dao;

    SignUpService(){
        dao = UserDAO.INSTANCE;
    }

    public int signUp(UserVO vo){
        try {
            return dao.insertUser(vo);
        } catch (Exception e) {
            throw new RuntimeException("회원가입 처리 중 오류 발생", e);
        }
    }
}
