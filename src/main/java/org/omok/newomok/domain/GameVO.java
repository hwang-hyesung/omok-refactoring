package org.omok.newomok.domain;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameVO {
    private int gameId;
    private GameStatus status;  // enum 타입으로 정의
    private String winnerId;
    private String player1;
    private String player2;

    // enum 정의
    public enum GameStatus {
        PLAYING,   // 게임 진행중
        WAITING,   // 대기중
        FINISHED,   // 종료됨
        ABORTED // 연결 끊김
    }
}