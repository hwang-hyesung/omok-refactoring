let isIdChecked = false;

document.addEventListener('DOMContentLoaded', function () {
    const bio = document.getElementById('bio');
    const counter = document.getElementById('bio_counter');

    /* bio text counter: 한줄 소개 길이 확인 */
    bio.addEventListener('input', () => {
        counter.textContent = `${bio.value.length} / 20`;
    });

    /* id-check: 회원가입 버튼 관련 */
    const checkButton = document.getElementById('id_check_button');
    const userIdInput = document.getElementById('userId');
    const idNotice = document.getElementById('id_notice');

    // 공백 입력시 공백 제거 (공백 못 들어가게)
    userIdInput.addEventListener('input', function () {
        this.value = this.value.replace(/\s/g, '');  // 모든 공백 제거
    });

    checkButton.addEventListener('click', function () {
        const userId = userIdInput.value;
        const regex = /^[a-zA-Z0-9]*$/;

        //빈 값일시 클릭 전에 막아준다.
        if (userId === "") {
            idNotice.textContent = "아이디를 입력해주세요.";
            idNotice.className = "form-notice no-match";
            isIdChecked = false;
            return;
        }

        // 정규식 유효성 검사 실패 시 중단
        if (!regex.test(userId)) {
            idNotice.textContent = "영문자와 숫자만 입력해주세요.";
            idNotice.className = "form-notice no-match";
            isIdChecked = false;
            return;
        }

        fetch(`/check-id?userId=${encodeURIComponent(userId)}`)
            .then(res => res.json())
            .then(data => {
                if (data.exists) {
                    idNotice.textContent = '이미 사용 중인 아이디입니다.';
                    idNotice.className = 'form-notice no-match';
                    isIdChecked = false;
                } else {
                    idNotice.textContent = '사용 가능한 아이디입니다!';
                    idNotice.className = 'form-notice match';
                    isIdChecked = true;
                }
            });
    });
    userIdInput.addEventListener('input', function () {
        idNotice.textContent = "영문자, 숫자 조합 12자 이내";
        idNotice.classList.remove("match", "no-match");
        idNotice.classList.add("guide");

        // 중복확인 결과를 무효화
        isIdChecked = false;  // 전역 변수로 관리
    });

    /* password check and register: 패스워드 체크 및 등록 */
    const pwd = document.getElementById('password');
    const rePwd = document.getElementById('re_password');
    const notice = document.getElementById('pwd_notice');
    const form = document.querySelector('form');

    function checkPasswordMatch() {
        const pwdVal = pwd.value;
        const rePwdVal = rePwd.value;

        if (pwdVal === '' && rePwdVal === '') {
            notice.textContent = '';
            notice.className = 'form-notice';
            return false;
        } else if (pwdVal === rePwdVal) {
            notice.textContent = '비밀번호가 같습니다.';
            notice.className = 'form-notice match';
            return true;
        } else {
            notice.textContent = '비밀번호가 일치하지 않습니다.';
            notice.className = 'form-notice no-match';
            return false;
        }
    }

    pwd.addEventListener('input', checkPasswordMatch);
    rePwd.addEventListener('input', checkPasswordMatch);

    form.addEventListener('submit', function (e) {
        const passwordsMatch = checkPasswordMatch();

        if (!passwordsMatch) {
            e.preventDefault();
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        if (!isIdChecked) {
            e.preventDefault();
            alert('아이디 중복 확인을 먼저 해주세요.');
            return;
        }
    });

    /* profile random */
    const button = document.querySelector(".avatar-random-btn");
    const profileImg = document.querySelector("#profile");
    const profileInput = document.querySelector("#profileNumber");

    button.addEventListener("click", () => {
        const randomNum = Math.floor(Math.random() * 5) + 1; // 1부터 6까지
        profileImg.src = `../../img/profile/${randomNum}.png`;
        profileInput.value = randomNum;
    });

    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get("error");

    /* sign-up error */
    if (error === "1") {
        showAlert("회원가입에 실패했습니다."); //이를 통해 alertMsg 내부의 text를 받은 message로 설정
    }

    function showAlert(message){
        document.getElementById("alertMsg").innerText = message; //innertext를 변경
        document.getElementById("customAlert").classList.remove("hidden");
    }

    window.closeAlert = function (){
        document.getElementById("customAlert").classList.add("hidden");
    }
});
