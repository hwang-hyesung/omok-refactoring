<div id="modal">
    <div id="board">
        <div id="text"></div>
        <div id="info">
            <div id="user_img"></div>
            <div id="explanation">
                <div>아이디: ${data.userId}</div>
                <div>${total}전 ${data.win}승 ${data.lose}패</div>
                <div id="bar">
                    <div id="win_bar">${data.win}</div>
                    <div id="lose_bar">${data.lose}</div>
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
