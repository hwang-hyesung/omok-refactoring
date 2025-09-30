import {sockets, myRole, getCurrentTurn, setCurrentTurn, currentTurn} from "../matching/matching.js";
import * as board from './board.js';
import {openModal} from "../result/result.js";

/* 게임 상태 변수
    INIT: 게임 초기화중 / ERROR: 에러 / STONE: 돌 놓기 / GAMEOVER: 게임 종류
 */

let types = ['INIT', 'STONE', 'GAMEOVER', 'ERROR', 'STATE'];

window.addEventListener("DOMContentLoaded", () => {
    if(sessionStorage.getItem("resultModal") === 'opened') {
        openModal(sessionStorage.getItem("resultModal"));
    }
});
/* 게임 시작 시 웹소켓 오픈 */
export function startGame(gameId) {
    // 기존 게임 소켓이 있다면 정리
    if (sockets.game && sockets.game.readyState === WebSocket.OPEN) {
        sockets.game.close();
    }
    
    sockets.game = new WebSocket(`ws://localhost:8080/game/${gameId}`);
    const user = JSON.parse(localStorage.getItem("loginInfo"));
    
    // 게임 ID와 역할을 세션 스토리지에 저장 (복원용)
    sessionStorage.setItem("gameId", gameId);
    sessionStorage.setItem("myRole", myRole);
    
    sockets.game.onopen = () => {
        console.log('gamesocket: 정상 연결');
        sockets.game.send(JSON.stringify({
            type: "JOIN",
            userId: user.id,
            role: myRole
        }));
    }
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
                userId: user.id,
                role: myRole
            }
            console.log("JOIN SEND " + sendMsg.role);
            sockets.game.send(JSON.stringify(sendMsg));
        } else if(type === types[1]) {
            //STONE
            board.board[data.row][data.col] = data.stone;
            if(data.stone === myRole){
                const stoneSound = new Audio("../../../music/stonesound.mp3");
                stoneSound.play();
            }
            board.drawStone(data.row, data.col, data.stone);
            setCurrentTurn(getCurrentTurn()===1?2:1);
        } else if(type === types[2]) {
            //GAMEOVER
            const res = data.winner===myRole;
            updateResult(gameId, res);
            openModal(res);
            
            // 게임 종료 시 세션 스토리지 정리
            sessionStorage.removeItem("gameId");
            sessionStorage.removeItem("myRole");
        } else if(type === types[3]) {
            //ERROR
            alert(data.message);
        } else if(type === types[4]){
            //STATE
            console.log("STATE - 서버에서 게임 상태 수신");
            setCurrentTurn(data.turn);
            board.updateBoardData(data.board);
            board.redrawStones();
        }
    };
}

export function sendStone(row, col) {
    if (sockets.game && sockets.game.readyState === WebSocket.OPEN) {
        sockets.game.send(JSON.stringify({
            type: 'STONE',
            row, col,
            stone: myRole
        }));
    }
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

// 새로고침 시 게임 복원 함수
export function restoreGame() {
    const gameId = sessionStorage.getItem("gameId");
    const role = sessionStorage.getItem("myRole");
    
    if (gameId && role) {
        console.log("게임 복원 시도: gameId=" + gameId + ", role=" + role);
        
        // 역할 복원
        if (typeof myRole !== 'undefined') {
            myRole = Number(role);
        }
        
        // 게임 재시작 (서버에서 상태를 받아옴)
        startGame(gameId);
        
        return true;
    }
    return false;
}



