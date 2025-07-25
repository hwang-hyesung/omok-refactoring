export function openModal(winnerId) {
    if(winnerId === user.userId){
        document.getElementById("result-img").backgroundImage = "/img/win-text.png";
        document.getElementById("user_img").backgroundImage = `/img/profile/${user.image}.png`;
    } else {
        document.getElementById("result-img").backgroundImage = "/img/lose-text.png";
        document.getElementById("user_img").backgroundImage = `/img/profile/${user.image}_sad.png`;
    }
    document.getElementById("result-modal").display = 'flex';
};