/* 매칭 창 띄우기 */

/* 게임 상태 변수
WAITING: 대기 상태
MATCHED: 매치된 상태
PLAYING: 게임 중
FINISHED: 완료 상태
ABORTED: 중단 상태 (EX. 도중에 창 끄기 등등)
 */

import { cache } from "../matching/matching.js";
import * as matchingJs from "../matching/matching.js";

let status = ['WAITING' , 'MATCHED', 'PLAYING', 'FINISHED', 'ABORTED'];

let type = ['move', 'gameover'];

export let currentTurn = 1; // 1 = 흑돌(선공), 2 = 백돌(후공)
export let myRole = 0; // 0 = 미할당, 1 = 흑돌, 2 = 백돌


/* 게임 시작 시 웹소켓 오픈 */
function openWebSocket(gameId) {
    const socket = new WebSocket(`ws://localhost:8080/min-value?gameId=${gameId}`);

    socket.onopen = () => console.log('websocket: 정상 연결');
    socket.onerror = () => console.log('websocket: 오류');
    socket.onclose = () => console.log('websocket: 닫힘');

    //메시지 처리
    socket.onmessage = function(e) {
        const data = JSON.parse(e.data);
        const status = data.status;

        if(status === status[0]) {
            //waiting: 상대방이 아직 존재하지 않는 경우
            handleWaitingStatus(data);
        } else if(status === status[1]) {
            //matched: 상대방이 들어와 매칭된 경우
            cache.opp = data.opponent;

            //돌 정하기
            myRole = (cache.you.id.trim() === data.player1.trim()) ? 1 : 2;
        } else if(status === status[2]) {
            //playing:
            const type = data.type;

            if(type === type[0]) {
                //move
            } else if(type === type[1]) {
                //game over
            }
        } else if(status === status[3]) {
            //finished
        } else if(status === status[4]) {
            //aborted
        }
    };
}

function handleWaitingStatus(data) {
    //1. 내 정보 넣기
    matchingJs.renderPlayer(1, cache.you);

    //2. 돌 색상 정하기 (player1: 흑돌 / player2: 백돌)
    matchingJs.renderPlayer(0, cache.opp);

    //3. 매칭 모달 띄우기
    matchingJs.openMatchingModal();
}