// 1) 전역 선언
const params   = new URLSearchParams(window.location.search);
const gameId   = params.get('gameId');
const myUserId = JSON.parse(sessionStorage.getItem('myServerIds') || '[]')[0] || '';
export const mid     = document.querySelector('.chat-mid');
export const btn   = document.querySelector('.send-btn');
export const input = document.querySelector('.chat-input');
// 2) 메시지 내역 렌더링 (초기 로드)
function loadMsg() {
    const mid     = document.querySelector('.chat-mid');
    mid.innerHTML = '';
    const history = JSON.parse(sessionStorage.getItem('chatHistory') || '[]');
    const myIds   = JSON.parse(sessionStorage.getItem('myServerIds') || '[]');

    history.forEach(({ senderId, text }) => {
        const div = document.createElement('div');
        div.className = myIds.includes(senderId) ? 'msg mine' : 'msg other';
        div.innerText = text;
        mid.appendChild(div);
    });

    mid.scrollTop = mid.scrollHeight;
}

// 3) 메시지 저장
function saveMsg(senderId, text) {
    const history = JSON.parse(sessionStorage.getItem('chatHistory') || '[]');
    history.push({ senderId, text });
    sessionStorage.setItem('chatHistory', JSON.stringify(history));
}

// 4) 화면에 채팅 말풍선 그리기
function appendChatBubble(senderId, text) {
    const mid   = document.querySelector('.chat-mid');
    const div   = document.createElement('div');
    const myIds = JSON.parse(sessionStorage.getItem('myServerIds') || '[]');
    div.className = myIds.includes(senderId) ? 'msg mine' : 'msg other';
    div.innerText  = text;
    mid.appendChild(div);
    mid.scrollTop  = mid.scrollHeight;
}

function appendBubble(mid, senderId, text) {
    const div = document.createElement('div');
    const myIds = JSON.parse(sessionStorage.getItem('myServerIds') || '[]');
    div.className = myIds.includes(senderId) ? 'my-message' : 'other-message';
    div.innerText = text;
    mid.appendChild(div);
}

// 5) 메시지 전송 함수
function sendMsg(input) {
    const text = input.value.trim();
    if (!text) return;

    const msgObj = { type: 'chat', senderId: myUserId, text };
    // 실제 WebSocket 전송은 websocket.js 의 sendChat 호출
    // sendChat(msgObj);

    appendChatBubble(myUserId, text);
    saveMsg(myUserId, text);
    input.value = '';
}

// 6) DOM 준비 후 초기화
window.addEventListener('DOMContentLoaded', () => {
    const input = document.querySelector('.chat-input');
    const btn   = document.querySelector('.send-btn');

    loadMsg();

    // 버튼 이벤트만 바인딩
    btn.addEventListener('click', () => sendMsg(input));
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') sendMsg(input);
    });
});
