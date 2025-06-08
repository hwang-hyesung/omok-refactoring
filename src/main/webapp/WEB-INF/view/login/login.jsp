<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <link rel="stylesheet" href="./login.css">
    <link rel="stylesheet" href="./login-error-modal.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/music/css/bgm-button-style.css">
    <script src="${pageContext.request.contextPath}/music/js/bgm-control.js"></script>
    <script src="${pageContext.request.contextPath}/music/js/sound-effect-control.js"></script>
    <script src="./login-error-modal.js"></script>
</head>
<style>
</style>
<body>
<%--음악 재생 버튼, 초기 설정은 off로 한다.--%>
<button id="music-btn" class="music-off"></button>
<audio id="bgm" src="${pageContext.request.contextPath}/music/bgm.mp3" loop></audio>
<audio id="click-sound" src="${pageContext.request.contextPath}/music/bubble-click-sound.mp3" preload="auto"></audio>

<%--여기에 custom alert을 기본 속성 hidden으로 둬야 한다.--%>
<div id="customAlert" class="hidden">
    <div class="alertBox-wrapper">
        <img src="${pageContext.request.contextPath}/img/id_error/id_error.png" class="alertBox-bg" />
        <div class="alert-content">
            <div id="alert_header">
                <img src="${pageContext.request.contextPath}/img/id_error/error_title.png" class="alert-title">
            </div>
            <p id="alertMsg">아이디 혹은 비밀번호가 다릅니다.</p>
            <button class="sound-button" id="alert_button" onclick="closeAlert()">확인</button>
        </div>
    </div>
</div>

<%--login 화면--%>
<div class="login-container">
    <div class="title">
        <img src="${pageContext.request.contextPath}/img/logo.png" alt="오목게임 로고">
    </div>
    <form action="/login" method="post">
        <input class="input-field" type="text" name="userId" placeholder="ID 입력해라" required>
        <input class="input-field" type="password" name="userPw" placeholder="PW 입력해라" required>
        <button type="submit" class="sound-button" id="submit-button">로그인</button>
    </form>
    <a class="register" href="/sign-up">회원가입</a>
</div>
</body>
</html>

