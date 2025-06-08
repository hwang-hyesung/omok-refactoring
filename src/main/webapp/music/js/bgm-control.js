/*
세션에 저장한다는 한계 때문에, 뒤로가기나 새로고침으로 문서를 다시 그릴 경우엔 자동 재생이 안된다.
이를 해결하기 위해선 React, view.js 등을 사용해야 한다고 한다...

우선은, session에 bgm-playing 상태와 bgm-time이라는 재생 시간을 저장해서 불러오는 방식으로 구현했다.
 */

window.addEventListener("DOMContentLoaded", function () {
    const bgm = document.getElementById("bgm");
    const btn = document.getElementById("music-btn");

    let isPlaying = sessionStorage.getItem("bgm-playing") === "true";

    // 볼륨 조절
    document.getElementById("bgm").volume = 0.2;

    if (isPlaying) {
        bgm.play().then(() => {
            const savedTime = parseFloat(sessionStorage.getItem("bgm-time") || "0");
            bgm.currentTime = savedTime;
        }).catch((e) => {
            console.log("음악 자동 재생이 차단됨", e);
            btn.classList.remove("music-on");
            btn.classList.add("music-off");
            sessionStorage.setItem("bgm-playing", "false");
        });

        btn.classList.remove("music-off");
        btn.classList.add("music-on");
    }

    btn.addEventListener("click", function () {
        if (isPlaying) {
            bgm.pause();
            btn.classList.remove("music-on");
            btn.classList.add("music-off");
            sessionStorage.setItem("bgm-playing", "false");
        } else {
            bgm.play();
            btn.classList.remove("music-off");
            btn.classList.add("music-on");
            sessionStorage.setItem("bgm-playing", "true");
        }
        isPlaying = !isPlaying;
    });
});

// 새로고침, 이동 전 위치 저장
window.addEventListener("beforeunload", function () {
    const bgm = document.getElementById("bgm");
    if (bgm && !bgm.paused) {
        sessionStorage.setItem("bgm-time", bgm.currentTime);
    }
});