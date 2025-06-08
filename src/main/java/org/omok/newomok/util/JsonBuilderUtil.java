package org.omok.newomok.util;

import com.google.gson.JsonObject;
import org.omok.newomok.domain.GameVO;
import org.omok.newomok.domain.UserVO;

public class JsonBuilderUtil {
    public static JsonObject getGameInfo(GameVO game) {
        JsonObject json = new JsonObject();
        json.addProperty("gameId", game.getGameId());
        json.addProperty("status", game.getStatus().toString());
        json.addProperty("player1", game.getPlayer1());
        json.addProperty("player2", game.getPlayer2());
        return json;
    }

    public static JsonObject getUserInfo(UserVO user) {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getUserId());
        json.addProperty("rate", user.getRate());
        json.addProperty("img", user.getImage());
        return json;
    }
}