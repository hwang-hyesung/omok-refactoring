// let hasShownModal = false;
export const sockets = {
    matching: null,
    game: null,
    chat: null,
};

export const cache = {
    you: null,
    opp: null
}

export let myRole = 0; //0: 미할당 / 1: 흑 / 2: 백
export let currentTurn = 1; //1: 흑돌(선공) / 2: 백돌(후공)

window.addEventListener("DOMContentLoaded", () => {
   //url에 gameId parameter 확인
   const urlParams = new URLSearchParams(window.location.search);
   const gameId = urlParams.get("gameId");
});

//매칭, 모달 컨트롤
function matchInit(gameId) {
    //gameId가 없는 경우: 오류 - 메인 페이지로 이동
    if(!gameId) {
        alert(`오류가 발생했습니다. 메인 페이지로 돌아갑니다.`);
        return;
    }

    /* 매칭 소켓 연결하여 방 상태 확인 */
    //1. 매칭 소켓 연결
    sockets.matchingSocket = new WebSocket(`ws://localhost:8080/min-value?${gameId}`);

    //2. 소켓 연결 상태 확인
    sockets.matchingSocket.onopen = () => console.log('websocket: 정상 연결');
    sockets.matchingSocket.onerror = () => console.log('websocket: 오류');
    sockets.matchingSocket.onclose = () => console.log('websocket: 닫힘');

    //3. 방 상태 확인
    sockets.matchingSocket.onmessage = function(e) {
        const data = JSON.parse(e.data);
        const status = data.status;

        if(status === 'WAITING') {
            //대기 상태인 경우
            //1. 캐시에 정보 저장
            cache.you = data.you
            cache.opp = data.opp || null;

            //2. 매칭 모달 띄우기
            handleWaitingStatus(data);
        } else if(status === 'MATCHED') {
            //매칭이 된 경우
            cache.you = data.you;

            myRole = (cache.you.id === data.player1) ? 1 : 2;
            currentTurn = 1;


        }
    }
    //gameId가 있는 경우: /omok/match로 POST 요청을 보내 매치 상태 확인

    // fetch('omok/match', {
    //     method: 'POST',
    //     headers: {'Content-Type': 'application/json'},
    //     body: JSON.stringify({gameId})
    // })
    //     .then(res => res.json())
    //     .then(data => {
    //         //상태 정보 불러옴: WAITING/MATCHED
    //         const status = data.game.status;
    //
    //         //유저 정보 저장
    //         cache.you = data.you;
    //         cache.opp = data.opp || null;
    //
    //         if(status === "WAITING") {
    //             console.log('대기중');
    //             showWaitingModal();
    //         }
    //         openWebSocket(gameId);
    //     })
}

//대기 상태인 경우 매칭 모달 띄우기
function handleWaitingStatus(you) {
    //1. 내 정보 넣기
    renderPlayer(1, you);

    //2. 돌 색상 주입하기: WAITING status는 player1만 받음
    setStones(you.id, you.id);

    //3. 모달에 waiting 띄우기
    showWaitingModal();

    //4. 모달 열기
    openMatchingModal();
}

//매치가 된 상태인 경우
function handleMatchesStatus(you, opp, player1Id) {
    //1. 정보 넣기
    renderPlayer(1, you);
    renderPlayer(2, opp);

    //2. 돌 색상 주입하기
    setStones(you.id, player1Id);

    //3. waiting 모달 없애고 player2 컴포넌트 띄우기
    showPlayer2Component()
}
//모달 창에서 플레이어 카드 정보 넣기
function renderPlayer(role, info) {
    /*
        role: you/opponent
        info: 해당 유저 정보
     */

    const nameId = Number(role) === 1 ? "name1" : "name2";
    const rateId = Number(role) === 1 ? "rate1" : "rate2";
    const imgId = Number(role) === 1 ? "profile1" : "profile2";

    document.getElementById(nameId).textContent = info.id;
    document.getElementById(rateId).textContent = `승률 ${info.rate} %`;
    document.getElementById(imgId).style.backgroundImage = `url('../../img/profile/${info.img}.png')`;
}

// 돌 색상 정하기
function setStones(youId, player1Id) {
    const isPlayer1 = youId === player1Id;

    const blackStone = document.querySelector(".stone-image-black");
    const whiteStone = document.querySelector(".stone-image-white");

    if(isPlayer1) {
        //내가 player1인 경우
        blackStone.src = "../../img/black_stone.png";
        whiteStone.src = "../../img/white_stone.png";
    } else {
        //내가 player2인 경우
        blackStone.src = "../../img/white_stone.png";
        whiteStone.src = "../../img/black_stone.png";
    }
}

//waiting 모달 띄우기
function showWaitingModal() {
    // if(hasShownWaiting) return;

    document.getElementById('card-bg2').classList.add('hidden');
    document.getElementById('stone2').classList.add('hidden');
    document.getElementById('profile2').classList.add('hidden');
    document.getElementById('name2').classList.add('hidden');
    document.getElementById('rate2').classList.add('hidden');

    //로딩 아이콘 생성
    document.getElementById('loading2').classList.remove('hidden');
}


//매치된 상태라면 -> 로딩 없애고 player2 정보 hidden 속성 삭제
function showPlayer2Component() {
    /* opp: 상대 정보
       player1: 흑돌 두는 플레이어 
       you: 내 정보
     */

    //플레이어2 카드 표시
    document.getElementById('card-bg2').classList.remove('hidden');
    document.getElementById('stone2').classList.remove('hidden');
    document.getElementById('profile2').classList.remove('hidden');
    document.getElementById('name2').classList.remove('hidden');
    document.getElementById('rate2').classList.remove('hidden');

    //로딩 아이콘 없애기
    document.getElementById('loading2').classList.add('hidden');
}

function openMatchingModal() {
    // if(hasShownModal) {
    //     return;
    // }
    document.getElementById("modalOverlay").style.display = "flex";

    // hasShownModal = true;
}
export function hideMatchingModal() {
    document.getElementById("modalOverlay").style.display = "none";
}

//게임화면에 프로필 띄우기
function showGameProfile(you, opp, player1Id) {
    const nameElements = document.getElementsByClassName('profile-player-name');
    const imgElements = document.querySelector('.game-profile-image2');

    if(player1Id === opp.id) {
        nameElements[1].textContent = you.id;
        imgElements[1].style.backgroundImage = `url('${contextPath}/img/profile/${you.img}.png')`;
    } else {
        nameElements[1].textContent = opp.id;
        imgElements[1].style.backgroundImage = `url('${contextPath}/img/profile/${opp.img}.png')`;
    }
}