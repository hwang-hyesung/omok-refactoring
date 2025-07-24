export const sockets = {
    matching: null,
    game: null,
    chat: null,
};

/*
    user: id/img/win/lose/rate
    opp: id/rate/img
 */

export let myRole = 0; //0: 미할당 / 1: 흑 / 2: 백
export let currentTurn = 1; //1: 흑돌(선공) / 2: 백돌(후공)


const user = JSON.parse(localStorage.getItem("loginInfo"));

window.addEventListener("DOMContentLoaded", () => {
   //url에 gameId parameter 확인
   const urlParams = new URLSearchParams(window.location.search);
   const gameId = urlParams.get("gameId");

   //매칭
   matchInit(gameId);

   //모달 상태 저장 - 모달 중복 렌더링 방지
    if (!sessionStorage.getItem("opened")) {
        sessionStorage.setItem("opened", "N");
    }
});

//매칭, 모달 컨트롤
export function matchInit(gameId) {
    //gameId가 없는 경우: 오류 - 메인 페이지로 이동
    if(!gameId) {
        alert(`오류가 발생했습니다. 메인 페이지로 돌아갑니다.`);
        return;
    }

    /* 매칭 소켓 연결하여 방 상태 확인 */
    //1. 매칭 소켓 연결
    sockets.matchingSocket = new WebSocket(`ws://localhost:8080/matching?gameId=${gameId}`);

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
            console.log('WAITING');

            //매칭 모달 띄우기
            handleWaitingStatus(user);

            //게임 창에 유저 정보 띄우기
            showGameProfile(user, 1);
            showGameProfile(null, 2);
        } else if(status === 'MATCHED') {
            //매칭이 된 경우
            openMatchingModal();

            const player1_temp = JSON.parse(data.player1);
            const player2_temp = JSON.parse(data.player2)   ;

            const player1 = {
                id: player1_temp.userId,
                img: player1_temp.image,
                win: player1_temp.win,
                lose: player1_temp.lose,
                rate: player1_temp.rate,
            }
            const player2 = {
                id: player2_temp.userId,
                img: player2_temp.image,
                win: player2_temp.win,
                lose: player2_temp.lose,
                rate: player2_temp.rate,
            }

            console.log(player1);
            console.log(player2);

            console.log('MATCHED');

            //모달창 제어
            handleMatchesStatus(user, player1, player2);

            //게임 창에 유저 정보 띄우기
            showGameProfile(player1, 1);
            showGameProfile(player2, 2);
        }
    }
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
function handleMatchesStatus(you, player1, player2) {
    //1. MyRole 결정
    let opp;
    let oppRole;
    if(you.id === player1.id) {
        myRole = 1;
        oppRole = 2;
        opp = player2;
    } else if(you.id === player2.id) {
        myRole = 2;
        oppRole = 1;
        opp = player1;
    }

    localStorage.setItem("oppInfo", opp);
    //2. currentTurn 설정
    currentTurn = 1;

    //3. 정보
    renderPlayer(myRole, you);
    renderPlayer(oppRole, opp);

    //4. 돌 색상 주입하기
    setStones(you.id, player1.id);

    //5. waiting 모달 없애고 player2 컴포넌트 띄우기
    showPlayer2Component();

    setTimeout(() => {
        hideMatchingModal();
    }, 4000);
}
//모달 창에서 플레이어 카드 정보 넣기
function renderPlayer(role, info) {
    /*
        role: you/opponent
        info: 해당 유저 정보
     */
    document.getElementById("name" + role).textContent = info.id;
    document.getElementById("rate" + role).textContent = `승률 ${info.rate} %`;
    document.getElementById("profile" + role).style.backgroundImage = `url('../../img/profile/${info.img}.png')`;
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
    console.log("에");
    console.log(sessionStorage.getItem("opened"));
    if(sessionStorage.getItem("opened") === "Y") {
        console.log("엥");
        return;
    }

    document.getElementById("modalOverlay").style.display = "flex";

    sessionStorage.setItem("opened", "Y");
}
export function hideMatchingModal() {
    document.getElementById("modalOverlay").style.display = "none";
}

//게임화면에 프로필 띄우기
function showGameProfile(player, role) {
    const nameElements = document.getElementsByClassName('profile-player-name');
    const imgElements = document.getElementsByClassName('profile-image');

    if(player === null) {
        nameElements[role - 1].textContent = "...";
        imgElements[role - 1].style.backgroundImage = `url('${contextPath}/img/profile/unknown.png')`;
    } else {
        nameElements[role - 1].textContent = player.id;
        imgElements[role - 1].style.backgroundImage = `url('${contextPath}/img/profile/${player.img}.png')`;
    }
}

