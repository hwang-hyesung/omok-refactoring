<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Sign Up</title>
    <link rel="stylesheet" href="./signup.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/music/css/bgm-button-style.css">

    <%--  회원가입 에러 모달 사용을 위해서 로그인 에러 모달 재활용...--%>
    <link rel="stylesheet" href="../login/login-error-modal.css">

    <script src="./signup.js"></script>
    <script src="${pageContext.request.contextPath}/music/js/bgm-control.js"></script>
    <script src="${pageContext.request.contextPath}/music/js/sound-effect-control.js"></script>
</head>
<body>
<button id="music-btn" class="music-off"></button>
<audio id="bgm" src="../../../music/bgm.mp3" loop></audio>
<audio id="click-sound" src="../../../music/bubble-click-sound.mp3" preload="auto"></audio>

<%--여기에 custom alert을 기본 속성 hidden으로 둬야 한다.--%>
<div id="customAlert" class="hidden">
    <div class="alertBox-wrapper">
        <img src="${pageContext.request.contextPath}/img/id_error/id_error.png" class="alertBox-bg" />
        <div class="alert-content">
            <div id="alert_header">
                <img src="${pageContext.request.contextPath}/img/id_error/error_title.png" class="alert-title">
            </div>
            <p id="alertMsg">회원가입에 실패했습니다.</p>
            <button class="sound-button" id="alert_button" onclick="closeAlert()">확인</button>
        </div>
    </div>
</div>

<div class="sign-up">
    <div class="sign-up__title">
        <span>S</span><span>i</span><span>g</span><span>n</span><span>U</span><span>p</span>
    </div>
    <div class="sign-up__form">
        <form action="/sign-up" method="post">
            <div class="sign-up__form-input">
                <div id="id_input_group">
                    <div id="id_input">
                        <label for="userId">아이디:</label>
                        <input type="text" id="userId" name="userId"
                               maxlength="12"
                               title="영문자와 숫자만 입력하세요"
                               required>
                        <button id="id_check_button" class="sound-button" type="button">중복 확인</button>
                    </div>
                    <div id="id_notice" class="form-notice">
                        영문자, 숫자 조합 12자 이내
                    </div>
                </div>
                <div id="password_group">
                    <div id="pwd_input">
                        <label for="password">비밀번호:</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    <div id="re_pwd_input">
                        <label for="re_password">재입력:</label>
                        <input type="password" id="re_password" name="re_password" required>
                    </div>
                    <div id="pwd_notice" class="form-notice"></div>
                </div>
                <div id="bio_group">
                    <div id="bio_input">
                        <label for="bio">한줄소개:</label>
                        <textarea id="bio" name="bio" rows="2" maxlength="20"></textarea>
                    </div>
                    <div id="bio_counter" class="form-counter">0 / 20</div>
                </div>
            </div>
            <div class="sign-up__form-profile">
                <div id="profile_input">
                    <div class="profile-wrapper">
                        <img id="profile" src="${pageContext.request.contextPath}/img/profile/1.png">
                        <button type="button" class="avatar-random-btn">🎲</button>
                    </div>
                    <input type="hidden" name="profileNumber" id="profileNumber" value="1">
                    <button id="submit_button" class="sound-button" type="submit">회원가입</button>
                </div>
            </div>
        </form>
    </div>
    <div class="sign-up__footer"></div>
</div>
</body>
</html>

