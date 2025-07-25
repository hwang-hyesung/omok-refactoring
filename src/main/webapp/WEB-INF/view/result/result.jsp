<script>
    const user = localStorage.getItem("loginInfo");
    const total = user.win + user.lose;
    const winRate = total > 0 ? Math.round(user.win / total * 100) : 0;
    const loseRate = total > 0 ? 100 - winRate : 0;
</script>
<div id="modal">
    <div id="container">
        <div id="result-img"></div>
        <div id="info">
            <div id="user_img"></div>
            <div id="explanation">
                <div>아이디: ${user.userId}</div>
                <div>${total}전 ${user.win}승 ${user.lose}패</div>
                <div id="bar">
                    <div id="win_bar">${user.win}</div>
                    <div id="lose_bar">${user.lose}</div>
                </div>
                <div id="bar_label">
                    <div id="win_label">승 (${winRate}%)</div>
                    <div id="lose_label">패 (${loseRate}%)</div>
                </div>
            </div>
        </div>
        <div id="btn">
            <button id="go_main_btn">메인 메뉴</button>
            <!--            <button id="re_btn">다시 시작</button>-->
        </div>
    </div>
</div>
