<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>404_error_page</title>
    <style>
        @font-face {
            font-family: 'NeoDunggeunmo';
            src: url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2001@1.3/NeoDunggeunmo.woff') format('woff');
            font-style: normal;
        }

        * {
            font-family: 'NeoDunggeunmo', sans-serif;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            margin: 0;
        }

        #logo {
            position: absolute;
            top: 5%;
            right: 3%;
            width: 10vw;
            height: 5vw;
            background: url('/img/logo.png') no-repeat center center;
            background-size: contain;
            z-index: 10;
        }

        #back {
            background: url('/img/background2.png') no-repeat center center;
            background-size: cover;
            height: 100vh;
            width: 100vw;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .content-wrapper {
            display: flex;
            flex-direction: column;
            align-items: center;
            transform: translateY(-10%);
            text-align: center;
            color: black;
            font-size: 2vw;
        }

        #fix {
            background: url('/img/fixing.png') no-repeat center center;
            background-size: contain;
            width: 40vw;
            height: 50vh;
            margin-bottom: 2vh;
        }
    </style>
</head>
<body>
    <div id="logo"></div>

    <div id="back">
        <div class="content-wrapper">
            <div id="fix"></div>
            <div class="message">현재 입력하신 주소를 찾을 수 없습니다</div>
        </div>
    </div>
</body>
</html>
