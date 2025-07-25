import { sockets, myRole, currentTurn} from "../matching/matching.js";

/* 게임 상태 변수
    INIT: 게임 초기화중 / ERROR: 에러 / STONE: 돌 놓기 / GAMEOVER: 게임 종류
 */

let status = ['INIT', 'ERROR', 'STONE', 'GAMEOVER'];

let type = ['JOIN', 'STONE'];

/* 게임 시작 시 웹소켓 오픈 */
function startGame(gameId) {
    sockets.game = new WebSocket(`ws://localhost:8080/game/${gameId}`);

    sockets.game.onopen = () => console.log('websocket: 정상 연결');
    sockets.game.onerror = () => console.log('websocket: 오류');
    sockets.game.onclose = () => console.log('websocket: 닫힘');

    //메시지 처리
    sockets.game.onmessage = function(e) {
        const data = JSON.parse(e.data);
        const status = data.status;

        if(status === status[0]) {
            //INIT
            /*
                type: JOIN
                role: 1 or 2
             */
            console.log(status[0]);
            const sendMsg = {
                type: 'JOIN',
                role: myRole
            }
            sockets.game.send(JSON.stringify(sendMsg));
        } else if(status === status[1]) {
            //ERROR
            console.log(status[1]);


        } else if(status === status[2]) {
            //STONE
            console.log(status[2]);
        } else if(status === status[3]) {
            //GAMEOVER
            console.log(status[3]);
        }
    };
}
