const pencilSrc = `/img/pencil_icon.png`;
const checkSrc = `/img/check_icon.png`;
let editing = false; //bio 수정 상태 flag

window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        console.log('뒤로가기로 접근됨 - 리로드');
        window.location.reload();
    }
});

document.addEventListener("DOMContentLoaded", function() {
    let normalImgUrl = "/img/profile/" + imageNum + ".png";
    let sadImgUrl = "/img/profile/" + imageNum + "_sad.png";

    /* 한줄 소개 변경 클릭 리스너*/
    document.getElementById('edit_icon').addEventListener('click', function() {
        updateBio(this);
    });

    /* 한줄 소개 박스 포커스 리스너*/
    document.querySelectorAll('textarea.bio_text').forEach(function(textarea) {
        textarea.addEventListener('focus', function () {
            setBioBorder(editing);
        });

        textarea.addEventListener('blur', function () {
            setBioBorder(!editing);
        });

        textarea.addEventListener('mousedown', function (e) {
            if (textarea.readOnly) {
                e.preventDefault();
            }
        });
    });

    /* 로그아웃 버튼 호버 리스너*/
    const logoutImg = document.querySelector('#logout_btn img');
    const avatar = document.getElementById('avatar');

    if (logoutImg && avatar) {
        logoutImg.addEventListener('mouseenter', function () {
            avatar.style.backgroundImage = `url(${sadImgUrl})`;
        });

        logoutImg.addEventListener('mouseleave', function () {
            avatar.style.backgroundImage = `url(${normalImgUrl})`;
        });
    }

    /* 시작버튼 리스너 */
    document.getElementById("start_btn").addEventListener('click', function(e) {
        e.preventDefault();

        const startBtn = this;
        startBtn.disabled = true;

        const clickSound = document.getElementById("click-sound");
        clickSound.currentTime = 0;
        clickSound.play();

        clickSound.onended = () => {
            startGame();
            startBtn.disabled = false;
        };
    });

    /* 랭킹(1-10위) 업데이트 */
    setRankingList(ranks);

    /* 내 랭킹 업데이트 */
    setMyRank(myRank, normalImgUrl, userId, winRate);

    /* 내 프로필 업데이트 */
    setProfile(userId, userBio, winNum, loseNum, imageNum);

    /* 그래프 업데이트 */
    setBar(winRate);
});


/* 한줄 소개 변경 함수 */
function updateBio(buttonEl) {
    let bio_text = $('.bio_text');

    if (!editing) {
        editing = true;
        bio_text.prop('readonly', false).focus();
        setBioBorder(editing);
        $(buttonEl).attr('src', checkSrc); // 체크 아이콘으로 변경
    } else {
        editing = false;
        bio_text.prop('readonly', true);
        setBioBorder(editing);
        $(buttonEl).attr('src', pencilSrc); // 다시 연필 아이콘으로 변경

        let newBio = bio_text.val().replace(/\n/g, "");
        bio_text.val(newBio);

        $.ajax({
            url: '/omok/updateBio',
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                userId: userId,
                bio: newBio
            }),
            success: function(response) {
                alert("한줄 소개 변경이 완료되었습니다.");
            },
            error: function(xhr, status, error) {
                console.error("한줄 소개 변경 실패:", error);
            }
        });
    }
}

/* 한줄 소개 박스 focus에 따른 보더 설정 함수 */
function setBioBorder(editing) {
    if(editing) {
        $('.value.bio').css('border', '2px solid #207600');
    } else {
        $('.value.bio').css('border', 'none');
    }
}

/* 개인정보 업데이트 함수 */
function setProfile(id, bio, win, lose, image_num) {
    $('.value.id')
        .text(id);
    $('.bio_text')
        .val(bio);
    $('.value-winning')
        .text((win + lose) + '전 ' + win + '승 ' + lose + '패');
    $('#avatar')
        .css('background-image', 'url(/img/profile/' + image_num + '.png');
}

/* 그래프 업데이트 함수 */
function setBar(winRate) {
    //승롤 바 업데이트
    $('.bar.win')
        .css('width', winRate + '%')
        .text(winRate + '%');
    //패배 바 업데이트
    $('.bar.lose')
        .css('width', (100 - winRate) + '%')
        .text((100 - winRate) + '%');

    if(winRate === 100) {
        $('.bar.lose')
            .text('');
    } else if(winRate === 0) {
        $('.bar.win')
            .text('');
    }
}

/* 마이 랭킹 업데이트 함수 */
function setMyRank(rank, imgUrl, userId, winRate) {
    const $myRank = $('#my_rank');

    $myRank.empty();

    const myRankHtml = `
        <div class="rank_num">${myRank}</div>
            <div class="rank_user_image_wrapper">
                    <img src="${imgUrl}" class="rank_user_image" alt="유저 프로필 이미지">
            </div>
            <div class="rank_user_id">${userId}</div>
            <div class="rank_user_rate">
                <img src="../../img/win_icon.png" class="win_icon" alt="승리아이콘">
                <span class="win_rate">${winRate}%</span>
            </div>`;

    $myRank.html(myRankHtml);
}

/* 랭킹 (1-10위) 업데이트 */
function setRankingList(ranks) {
    const $rankingSection = $('#ranking_section');

    $rankingSection.empty();

    ranks.forEach(rankItem => {
        const isMyself = rankItem.userId === userId;

        let rankHtml = `
            <div class="rank_item">
                <div class="rank_item_background ${isMyself ? 'highlight' : ''}">
                    <div class="rank_num">${rankItem.rank}</div>
                    <div class="rank_user_image_wrapper">
                        <img src="/img/profile/${rankItem.imageNum}.png" class="rank_user_image" alt="유저 프로필 이미지">
                    </div>
                    <div class="rank_user_id">${rankItem.userId}</div>
                    <div class="rank_user_rate">
                        <img src="/img/win_icon.png" class="win_icon" alt="승리아이콘">
                        <span class="win_rate">${rankItem.rate}%</span>
                    </div>
                </div>
            </div>
        `;
        $rankingSection.append(rankHtml);
    })
}

function startGame() {
    // 로그인 안 되어 있을 경우 이동해야 해서 이렇게 잡아둠.
    fetch("/omok/match", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({})
    })
        .then(res => {
            if (res.redirected) {
                window.location.href = res.url;
                return;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return; // 위에서 리다이렉트 되었으면 중단됨
            //로그 찍기 용
            console.log("서버 응답:", data); // 🔍 응답 구조 확인용
            const gameId = data.game.gameId;
            //로케이션 경로 변경
            location.href = `/omok/play?gameId=${gameId}`;
        });
}