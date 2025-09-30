import { sockets } from "../matching/matching.js";

const STATUS = { INIT: "INIT", CHAT: "CHAT", GAMEOVER: "GAMEOVER" };

let mid, btn, input;

// 유저 정보
const user = (() => {
    try {
        return JSON.parse(localStorage.getItem("loginInfo") || "{}");
    } catch {
        return {};
    }
})();

// gameId
const params = new URLSearchParams(window.location.search);
const gameId = params.get("gameId");

// 이벤트 중복 바인딩 가드
let handlersBound = false;

// 안전 close
function closeIfOpen(code = 1000, reason = "leaving") {
    try {
        if (sockets.chat && sockets.chat.readyState === WebSocket.OPEN) {
            sockets.chat.close(code, reason);
        }
    } catch (_) {}
}

// 페이지 이탈 시 명시적 close (beforeunload + unload + visibilitychange)
window.addEventListener("beforeunload", () => closeIfOpen(1000, "beforeunload"));
window.addEventListener("unload", () => closeIfOpen(1000, "unload"));
document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") closeIfOpen(1000, "hidden");
});

// 채팅 히스토리 렌더링
function loadMsg(midEl, userObj) {
    const myId = String(userObj?.id ?? "");
    midEl.innerHTML = "";
    const history = JSON.parse(sessionStorage.getItem("chatHistory") || "[]");
    history.forEach(({ senderId, text }) => {
        const div = document.createElement("div");
        div.className = String(senderId) === myId ? "my-message" : "other-message";
        div.innerText = text;
        midEl.appendChild(div);
    });
    midEl.scrollTop = midEl.scrollHeight;
}

// 메시지 저장
function saveMsg(senderId, text) {
    const history = JSON.parse(sessionStorage.getItem("chatHistory") || "[]");
    history.push({ senderId, text });
    sessionStorage.setItem("chatHistory", JSON.stringify(history));
}

// 채팅 버블 추가
function appendBubble(midEl, senderId, text, userObj) {
    const div = document.createElement("div");
    const myId = String(userObj?.id ?? "");
    div.className = String(senderId) === myId ? "my-message" : "other-message";
    div.innerText = text;
    midEl.appendChild(div);
    midEl.scrollTop = midEl.scrollHeight;
}

// 서버로 채팅 전송
function sendMsg(socket, inputEl, userObj) {
    if (!socket || socket.readyState !== WebSocket.OPEN) return;

    const text = inputEl.value.trim();
    if (!text) return;

    const payload = {
        senderId: String(userObj?.id ?? ""),
        message: text,
    };

    socket.send(JSON.stringify(payload));
    inputEl.value = "";
}

//소켓 시작
export function startChat(gid) {
    // 열려 있던 소켓이 있으면 정리 후 재연결
    closeIfOpen(1000, "reconnect");

    const uid = String(user?.id);
    const url = `ws://localhost:8080/chat/${gid}?userId=${encodeURIComponent(uid)}`;

    sockets.chat = new WebSocket(url);

    sockets.chat.onopen = () => console.log("chat socket: 정상 연결");
    sockets.chat.onerror = (e) => console.log("chat socket: 오류", e);
    sockets.chat.onclose = () => console.log("chat socket: 닫힘");

    sockets.chat.onmessage = (e) => {
        let data;
        try {
            data = JSON.parse(e.data);
        } catch {
            return;
        }

        if (data.status === STATUS.INIT) {
            console.log("chat socket:", STATUS.INIT);
            return;
        }

        if (data.status === STATUS.CHAT) {
            // 화면 렌더
            appendBubble(mid, data.senderId, data.text, user);
            // 스토리지 저장
            saveMsg(data.senderId, data.text);
            return;
        }

        if (data.status === STATUS.GAMEOVER) {
            console.log("chat socket:", STATUS.GAMEOVER);
            removeChat();
            closeIfOpen(1000, "gameover");
        }
    };
}

// 채팅 삭제
function removeChat() {
    sessionStorage.removeItem("chatHistory");
    if (mid) mid.innerHTML = "";
}

// DOMContentLoaded 이후 초기화
window.addEventListener("DOMContentLoaded", () => {
    mid = document.querySelector(".chat-mid");
    btn = document.querySelector(".send-btn");
    input = document.querySelector(".chat-input");

    if (!mid || !btn || !input) {
        console.warn("[chat] 필수 DOM 요소가 없습니다.");
        return;
    }

    loadMsg(mid, user);

    if (!handlersBound) {
        btn.addEventListener("click", () => sendMsg(sockets.chat, input, user));
        input.addEventListener("keypress", (e) => {
            if (e.key === "Enter") sendMsg(sockets.chat, input, user);
        });
        handlersBound = true;
    }

    // 최초 진입 시 자동 연결
    if (gameId) {
        startChat(gameId);
    }
});
