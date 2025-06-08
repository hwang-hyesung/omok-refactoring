package org.omok.newomok.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.omok.newomok.domain.UserVO;
import org.omok.newomok.service.UpdateService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

@Log4j2
@WebServlet(displayName = "updateController", urlPatterns = "/omok/updateBio")
public class UpdateController extends HttpServlet {
    private final UpdateService updateService = UpdateService.INSTANCE;

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String jsonData = sb.toString();

        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

        String userId = jsonObject.get("userId").getAsString();
        String bio = jsonObject.get("bio").getAsString();

        boolean result = updateService.modifyBio(userId, bio);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":" + result + "}");

        HttpSession session = request.getSession();
        UserVO userVO = (UserVO) session.getAttribute("loginInfo");

        userVO.setBio(bio);
        session.setAttribute("loginInfo", userVO);
    }
}
