package org.omok.newomok.service;

import lombok.extern.log4j.Log4j2;
import org.omok.newomok.repository.UpdateDAO;

@Log4j2
public enum UpdateService {
    INSTANCE;

    final private UpdateDAO updateDAO = UpdateDAO.INSTANCE;

    /* 한줄 소개 수정 함수 */
    public boolean modifyBio(String userId, String newBio) {
        return updateDAO.modifyBio(userId, newBio);
    }
}
