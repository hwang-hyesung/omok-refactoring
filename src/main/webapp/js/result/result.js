
const user = JSON.parse(localStorage.getItem("loginInfo"));
export function openModal(res) {

    const win = parseInt(user.win || 0, 10);
    const lose = parseInt(user.lose || 0, 10);
    const total = win + lose;
    const loseRate = (100-user.rate).toString();

    // DOM에 데이터 넣기
    document.getElementById("user_id_text").innerHTML = `${user.id}`;
    document.getElementById("record_text").innerHTML = `${total}전 ${user.win}승 ${user.lose}패`;
    document.getElementById("win_bar").style.width = Number(user.rate)+'%';
    document.getElementById("lose_bar").style.width = loseRate+'%';
    document.getElementById("win_bar").innerHTML = user.rate+'%';
    document.getElementById("lose_bar").innerHTML = loseRate+'%';
    document.getElementById("win_label").innerHTML = `승 (${user.rate}%)`;
    document.getElementById("lose_label").innerHTML = `패 (${loseRate}%)`;

    if(res){
        document.getElementById("result-img").style.backgroundImage = `url('/img/win_text.png')`;
        document.getElementById("user_img").style.backgroundImage = `url('/img/profile/${user.img}.png')`;
        const resSound = new Audio("../../../music/Power up.mp3");
        resSound.play();
    } else {
        document.getElementById("result-img").style.backgroundImage = `url('/img/lose_text.png')`;
        document.getElementById("user_img").style.backgroundImage = `url('/img/profile/${user.img}_sad.png')`;
        const resSound = new Audio("../../../music/Power up.mp3");
        resSound.play();
    }
    document.getElementById("result-modal").style.display = 'flex';
}

document.getElementById("go_main_btn").addEventListener("click", (e) => {
    localStorage.removeItem("oppInfo");
    sessionStorage.clear();
    window.location.href = "/omok/main";
});