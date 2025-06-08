package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.repository.userDAO;

@Log4j2
public enum SignUpService {
    INSTANCE;
    private userDAO dao;

    SignUpService(){
        dao = userDAO.INSTANCE;
    }

    public int signUp(UserVO vo){
        try {
            return dao.insertUser(vo);
        } catch (Exception e) {
            //여기서 받아서 exception 처리하기
            throw new RuntimeException("회원가입 처리 중 오류 발생", e);  // 또는 custom 예외로 감싸기
        }
    }
}

}
