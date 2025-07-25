<script>
    const user = {
        userId: '${sessionScope.loginInfo.userId}',
        total: ${sessionScope.loginInfo.totalGames},
        win: ${sessionScope.loginInfo.winCount},
        lose: ${sessionScope.loginInfo.loseCount}
    };
    console.log(user);
</script>
<div id="modalOverlay" class="modal-overlay" style="display:none;">
    <!-- 모달 박스 -->
    <div class="modal-content">
        <div class="match-image"></div>

        <div class="info-content">
            <!-- Player 1 -->
            <div class="player">
                <div class="card-bg"></div>
                <div class="player-card">
                    <div class="profile-image1" id="profile1"></div>
                    <div class="player-name" id="name1"></div>
                    <div class="player-rate" id="rate1"></div>
                </div>
                <img id="stone1" src="${pageContext.request.contextPath}/img/black_stone.png" class="stone-image-black" alt="흑돌"/>
            </div>

            <div class="vs-text">vs</div>

            <!-- Player 2 -->
            <div class="player" id="player2-wrapper">
                <img id="stone2" src="${pageContext.request.contextPath}/img/white_stone.png" class="stone-image-white hidden" alt="백돌"/>
                <div class="card-bg2 hidden" id="card-bg2"></div>
                <div class="player-card">
                    <!-- 로딩 아이콘은 waiting일 땐 보임, matched일 땐 숨김 -->
                    <div class="player-loading" id="loading2">
                        <img src="${pageContext.request.contextPath}/img/loading.png" alt="로딩중" class="loading-icon">
                    </div>
                    <!-- 상대방 정보는 waiting일 땐 숨김, matched일 땐 보임 -->
                    <div class="profile-image2 hidden" id="profile2"></div>
                    <div class="player-name hidden" id="name2"></div>
                    <div class="player-rate hidden" id="rate2"></div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="module" src="${pageContext.request.contextPath}/js/matching/matching.js"></script>