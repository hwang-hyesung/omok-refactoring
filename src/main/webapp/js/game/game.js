import {sockets, myRole, getCurrentTurn, setCurrentTurn, currentTurn} from "../matching/matching.js";
import * as board from './board.js';
import {openModal} from "../result/result.js";
import {loadBoardFromSession} from "./board.js";
/* 게임 상태 변수
    INIT: 게임 초기화중 / ERROR: 에러 / STONE: 돌 놓기 / GAMEOVER: 게임 종류
 */

let types = ['INIT', 'STONE', 'GAMEOVER', 'ERROR'];

/* 게임 시작 시 웹소켓 오픈 */
export function startGame(gameId) {
    sockets.game = new WebSocket(`ws://localhost:8080/game/${gameId}`);

    sockets.game.onopen = () => console.log('gamesocket: 정상 연결');
    sockets.game.onerror = () => console.log('gamesocket: 오류');
    sockets.game.onclose = () => console.log('gamesocket: 닫힘');

    //메시지 처리
    sockets.game.onmessage = function(e) {
        const data = JSON.parse(e.data);
        const type = data.type;

        if(type === types[0]) {
            //INIT
            const sendMsg = {
                type: 'JOIN',
                role: myRole
            }
            console.log("JOIN SEND" + sendMsg.role);
            sockets.game.send(JSON.stringify(sendMsg));
        } else if(type === types[1]) {
            //STONE
            board.board[data.row][data.col] = data.stone;
            board.drawStone(data.row, data.col, data.stone);
            setCurrentTurn(getCurrentTurn()===1?2:1);
            board.saveBoardToSession(getCurrentTurn());
        } else if(type === types[2]) {
            //GAMEOVER
            const res = data.winner===myRole;
            updateResult(gameId, res);
            openModal(res);
        } else if(type === types[3]) {
            //ERROR
            alert(data.message);
        }
    };

}

export function sendStone(row, col) {
    sockets.game.send(JSON.stringify({
        type: 'STONE',
        row, col,
        stone: myRole
    }));
}

function updateResult(gameId, res){
    const user = JSON.parse(localStorage.getItem("loginInfo"));
    const oppUser = JSON.parse(localStorage.getItem("oppInfo"));
    const winnerId= res ? user.id : oppUser.id;
    console.log("updateResult");
    $.ajax({
        url: '/omok/play',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            gameId,
            winnerId
        }),
        success: function(response) {
            console.log("DB 업데이트 완료", response);
        },
        error: function(xhr, status, error) {
            console.error("한줄 소개 변경 실패:", error);
        }
    });
}



