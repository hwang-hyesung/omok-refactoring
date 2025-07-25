import { sockets, myRole } from "../matching/matching.js";
import * as board from './board.js';
import {openModal} from "../result/result";
import {loadBoardFromSession} from "./board.js";
/* 게임 상태 변수
    INIT: 게임 초기화중 / ERROR: 에러 / STONE: 돌 놓기 / GAMEOVER: 게임 종류
 */

let types = ['INIT', 'STONE', 'GAMEOVER', 'ERROR'];
export let currentTurn = 1;

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
            console.log(types[0]);
            myRole = data.role;
            const sendMsg = {
                type: 'JOIN',
                role: myRole
            }
            sockets.game.send(JSON.stringify(sendMsg));
        } else if(type === types[1]) {
            //STONE
            console.log(types[1]);
            board.board[data.row][data.col] = data.color;
            board.drawStone(data.row, data.col, data.color);
            currentTurn = data.color === 1 ? 2 : 1;
            board.saveBoardToSession(currentTurn);
        } else if(type === types[2]) {
            //GAMEOVER
            console.log(types[2]);
            openModal(data.winner);
        } else if(type === types[3]) {
            //ERROR
            console.log(types[3]);
            alert(data.message);
        }
    };

    currentTurn = loadBoardFromSession();
}

export function sendStone(row, col) {
    if (myRole !== currentTurn || board.board[row][col] !== 0 || myRole === 0) return;

    board.board[row][col] = myRole;
    board.drawStone(row, col, myRole);
    board.saveBoardToSession(currentTurn);

    currentTurn = myRole === 1 ? 2 : 1;

    sockets.game.send(JSON.stringify({
        type: 'STONE',
        row, col,
        color: myRole
    }));
}



