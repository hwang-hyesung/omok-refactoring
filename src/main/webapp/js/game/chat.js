// 1) 전역 선언
import { sockets } from "../matching/matching.js";

const params   = new URLSearchParams(window.location.search);
const gameId   = params.get('gameId');
const myUserId = JSON.parse(sessionStorage.getItem('myServerIds') || '[]')[0] || '';
export const mid     = document.querySelector('.chat-mid');
export const btn   = document.querySelector('.send-btn');
export const input = document.querySelector('.chat-input');

let status = ['INIT', 'CHAT', 'GAMEOVER'];
const user = JSON.parse(localStorage.getItem("loginInfo"));

export function startChat(gameId) {
    sockets.chat = new WebSocket(`ws://localhost:8080/chat/${gameId}`);

    sockets.chat.onopen = () => console.log('chat socket: 정상 연결');
    sockets.chat.onerror = () => console.log('chat socket: 오류');
    sockets.chat.onclose = () => console.log('chat socket: 닫힘');

    sockets.chat.onmessage = function(e) {
        const data = JSON.parse(e.data);

        if(data.status === status[0]) {
            console.log("chat socket: " + status[0]);
        } if(data.status === status[1]) {
            //CHAT
            console.log("chat socket: " + status[1]);

            //화면에 채팅 렌더링
            appendBubble(mid, data.senderId, data.text, user);

            //스토리지에 채팅 저장
            saveMsg(data.senderId, data.text);
        } else if(data.status === status[2]) {
            //GAMOVER
            console.log("chat socket: " + status[2]);

            //기록 지우기
            removeChat();

            //소켓 닫기
            sockets.chat.close();
        }

    }
}

// 채팅 히스토리 렌더링
function loadMsg(mid, user) {
    const myId = user.id;
    mid.innerHTML = '';
    const history = JSON.parse(sessionStorage.getItem('chatHistory') || '[]');
    history.forEach(({ senderId, text }) => {
        const div = document.createElement('div');
        div.className = senderId === (myId) ? 'my-message' : 'other-message';
        div.innerText = text;
        mid.appendChild(div);
    });
    mid.scrollTop = mid.scrollHeight;
}

// 메시지 저장
function saveMsg(senderId, text) {
    const history = JSON.parse(sessionStorage.getItem('chatHistory') || '[]');
    history.push({ senderId, text });
    sessionStorage.setItem('chatHistory', JSON.stringify(history));
}

//서버에 채팅 전송
function sendMsg(socket, input, user) {
    const text = input.value.trim();
    if (!text) return;
    const payload = {
        senderId : user.id || '',
        message : text
    };
    socket.send(JSON.stringify(payload));
    console.log(payload);
    input.value = '';
}



function appendBubble(mid, senderId, text, user) {
    const div = document.createElement('div');
    // senderId 가 내 ID 면 .my-message, 아니면 .other-message
    div.className = (senderId === user.id) ? 'my-message' : 'other-message';
    div.innerText = text;
    mid.appendChild(div);
    mid.scrollTop = mid.scrollHeight;
}


// 6) DOM 준비 후 초기화
window.addEventListener('DOMContentLoaded', () => {
    const input = document.querySelector('.chat-input');
    const btn   = document.querySelector('.send-btn');

    loadMsg(mid, user);

    // 버튼 이벤트만 바인딩
    btn.addEventListener('click', () => sendMsg(sockets.chat, input, user));
    input.addEventListener('keypress', e => {
        if (e.key === 'Enter') sendMsg(sockets.chat, input, user);
    });
});

// 채팅 삭제
function removeChat() {
    sessionStorage.removeItem('chatHistory');
    mid.innerHTML = "";
}

