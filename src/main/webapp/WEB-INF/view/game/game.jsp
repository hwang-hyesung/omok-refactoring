<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OMOK</title>
    <link rel="stylesheet" href="../../../css/game/game.css" />
    <link rel="stylesheet" href="../../../css/game/profile.css" />
    <link rel="stylesheet" href="../../../css/game/chat.css"/>
</head>
<body>
<jsp:include page="../matching/matching.jsp" flush="false"/>
<jsp:include page="../result/result.jsp" flush="false"/>

<div class="container">
    <div class="board" id="board">
        <img id="board-image" src="${pageContext.request.contextPath}/img/omok_board.png" alt="omok board" />
    </div>
    <div class="info">
        <!-- 프로필 -->
        <div class="profile-content">
            <div class="profile-player">
                <div class="card-bg"></div>
                <div class="profile-player-card">
                    <!-- profile-image1: 배경 이미지로 처리 -->
                    <div class="profile-image"></div>
                    <div class="profile-player-name"></div>
                </div>
                <img src="${pageContext.request.contextPath}/img/black_stone.png" class="stone-image-black" alt="흑돌"/>
            </div>

            <div class="vs-text">vs</div>

            <div class="profile-player">
                <div class="card-bg"></div>
                <img src="${pageContext.request.contextPath}/img/white_stone.png" class="stone-image-white" alt="백돌"/>
                <div class="profile-player-card">
                    <!-- profile-image2: 배경 이미지로 처리 -->
                    <div class="profile-image"></div>
                    <div class="profile-player-name"></div>
                </div>
            </div>
        </div>


        <!-- 채팅 -->
        <div class="chat-box">
            <div class="chat-top">채팅방</div>
            <div class="chat-mid"></div>
            <div class="chat-bottom">
                <input type="text" class="chat-input" placeholder="메시지를 입력해 주세요">
                <button class="send-btn">전송</button>
            </div>
        </div>
    </div>
</div>

<script type="module" src="/js/game/chat.js"></script>
<script type="module" src="/js/game/board.js"></script>
</body>
</html>
