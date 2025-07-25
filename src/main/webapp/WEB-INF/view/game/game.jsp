<%--우리 게임 하는 화면은 WEB-INF에 넣을 경우, 여기 링킹하는거 수정해야 한다!!!! (Controller에서 하는거로.... 그래서 시간 없으면 그냥 빼둬도 괜찮다.)--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import ="org.omok.newomok.domain.UserVO"%>
<%@ page import ="org.omok.newomok.domain.GameVO" %>
<%@ page import ="org.omok.newomok.repository.UserDAO"%>
<%@ page import ="org.omok.newomok.repository.GameDAO"%>
<%@ page import="org.omok.newomok.repository.MatchDAO" %>
<%
    // 로그인 유저 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginInfo");
    String userId = loginUser != null ? loginUser.getUserId() : "unknown";

    // gameId 파라미터 확인
    String gameIdParam = request.getParameter("gameId");
    int gameId = -1;

    // player 정보 초기화
    String player1Id = "unknown";
    String player2Id = "unknown";
    int player1Img = -1, player2Img = -1;
    int player1Win = 0, player1Lose = 0;
    int player2Win = 0, player2Lose = 0;
    GameVO.GameStatus gameStatus = GameVO.GameStatus.WAITING;

    if (gameIdParam != null) {
        try {
            gameId = Integer.parseInt(gameIdParam);
            GameVO game = MatchDAO.getGameById(gameId);

            if (game != null) {
                player1Id = game.getPlayer1();
                player2Id = game.getPlayer2();
                gameStatus = game.getStatus();

                // player1 정보
                UserVO player1 = UserDAO.INSTANCE.getUserById(player1Id);
                if (player1 != null) {
                    player1Img = player1.getImage();
                    player1Win = player1.getWin();
                    player1Lose = player1.getLose();
                }

                // player2 정보
                UserVO player2 = UserDAO.INSTANCE.getUserById(player2Id);
                if (player2 != null) {
                    player2Img = player2.getImage();
                    player2Win = player2.getWin();
                    player2Lose = player2.getLose();
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(); // 로그로 남기기
        }
    }

    // 승률 계산
    int player1Total = player1Win + player1Lose;
    int player2Total = player2Win + player2Lose;
    int player1Rate = player1Total > 0 ? (int)((double) player1Win / player1Total * 100) : 0;
    int player2Rate = player2Total > 0 ? (int)((double) player2Win / player2Total * 100) : 0;
%>
<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OMOK</title>
    <link rel="stylesheet" href="../../../css/game/game.css" />
    <link rel="stylesheet" href="../../../css/game/profile.css" />
    <link rel="stylesheet" href="../../../css/game/chat.css"/>
    <%--    <link rel="stylesheet" href="${pageContext.request.contextPath}/view/game/board/board.css"/>--%>
    <%--매칭 모달 스타일 추가--%>
    <link rel="stylesheet" type="text/css" href="../../../css/matching/matching.css"/>
    <link rel="stylesheet" type="text/css" href="../../../css/result/result.css"/>
</head>
<body>

<%--여기에 매칭 모달 추가--%>
<!-- 모달 오버레이 -->
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
                <img src="<%= request.getContextPath() %>/img/black_stone.png" class="stone-image-black" alt="흑돌"/>
            </div>

            <div class="vs-text">vs</div>

            <div class="profile-player">
                <div class="card-bg"></div>
                <img src="<%= request.getContextPath() %>/img/white_stone.png" class="stone-image-white" alt="백돌"/>
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

<script type="module" src="../../../js/game/start-game.js"></script>

<%--&lt;%&ndash;여기에 웹소켓 + 매칭 관련 스크립트 추가&ndash;%&gt;--%>
<%--<script type="module" src="${pageContext.request.contextPath}/view/sunJ_maching/js/match/modal-ui.js"></script>--%>
<%--<script type="module" src="${pageContext.request.contextPath}/view/sunJ_maching/js/websocket.js"></script>--%>
<%--<script type="module" src="${pageContext.request.contextPath}/view/sunJ_maching/js/match/match-init.js"></script>--%>

</body>
</html>
