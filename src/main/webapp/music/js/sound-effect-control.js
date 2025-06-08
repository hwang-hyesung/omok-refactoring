/*
효과음 컨트롤러
 */
window.addEventListener("DOMContentLoaded", function () {
    //효과음 관련
    const clickSound = document.getElementById("click-sound");
    const buttons = document.querySelectorAll(".sound-button");

    // 볼륨 조절
    document.getElementById("click-sound").volume = 1.0;

    buttons.forEach(btn => {
        btn.addEventListener("click", () => {
            clickSound.currentTime = 0;
            clickSound.play();
        });
    });
});