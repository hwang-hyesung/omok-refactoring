//오목 돌 배치
import {myRole} from "./game.js";

export const boardSize = 15;
export const board = Array.from({ length: boardSize }, () => Array(boardSize).fill(0));
export let currentTurn = 1;

export const boardElement = document.getElementById("board");
export const boardImage = document.getElementById("board-image");

export const borderRatio = 65 / 768;
export const offsetX = -4;
export const offsetY = -2;

export let gridStartX, gridStartY, cellSizeX, cellSizeY;

export let hoverStone = null;

// 보드 크기 및 셀 크기
export function calculateGridMetrics() {
    const rect = boardImage.getBoundingClientRect();
    const boardWidth = rect.width;
    const boardHeight = rect.height;

    gridStartX = boardWidth * borderRatio + offsetX;
    gridStartY = boardHeight * borderRatio + offsetY;

    const gridSizeX = boardWidth - 2 * gridStartX;
    const gridSizeY = boardHeight - 2 * gridStartY;

    cellSizeX = gridSizeX / (boardSize - 1);
    cellSizeY = gridSizeY / (boardSize - 1);
}

export function getCellFromMousePosition(x, y) {
    // 보드 범위 체크
    if (
        x < gridStartX || x > gridStartX + cellSizeX * (boardSize - 1) ||
        y < gridStartY || y > gridStartY + cellSizeY * (boardSize - 1)
    ) return null;

    const col = Math.round((x - gridStartX) / cellSizeX);
    const row = Math.round((y - gridStartY) / cellSizeY);

    if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) return null;

    return { row, col };
}

// 새로고침 하면 세션에 저장한 돌 정보 불러오기
window.onload = () => {
    calculateGridMetrics();

    const savedBoard = sessionStorage.getItem('board');
    const savedTurn = sessionStorage.getItem('turn');

    if (savedBoard) {
        const parsedBoard = JSON.parse(savedBoard);
        for (let r = 0; r < boardSize; r++) {
            for (let c = 0; c < boardSize; c++) {
                board[r][c] = parsedBoard[r][c];
                if (board[r][c] !== 0) {
                    renderStone(r, c, board[r][c]);
                }
            }
        }
    }

    if (savedTurn) {
        currentTurn = parseInt(savedTurn);
    }
};

window.addEventListener('resize', () => {
    calculateGridMetrics();
    rerenderStones();
    if (hoverStone) {
        hoverStone.style.display = 'none'; // 화면 크기 변경 시 hover 숨김
    }
});

boardElement.addEventListener("mousemove", (e) => {
    if (!hoverStone) createHoverStone();
    if (myRole !== currentTurn) {
        if (hoverStone) hoverStone.style.display = 'none';
        return;
    }
    const rect = boardImage.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const cell = getCellFromMousePosition(x, y);

    if (!cell || board[cell.row][cell.col] !== 0) {
        hoverStone.style.display = 'none';
        return;
    }

    hoverStone.style.display = 'block';

    const left = gridStartX + cell.col * cellSizeX;
    const top = gridStartY + cell.row * cellSizeY;

    hoverStone.style.left = `${left}px`;
    hoverStone.style.top = `${top}px`;
    hoverStone.className = `stone hover ${currentTurn === 1 ? 'black' : 'white'}`;
});

boardElement.addEventListener("mouseleave", () => {
    if (hoverStone) hoverStone.style.display = 'none';
});

//클릭시 돌 두기
boardElement.addEventListener("click", (e) => {
    const rect = boardImage.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const cell = getCellFromMousePosition(x, y);
    if (!cell) return;

    placeStone(cell.row, cell.col);
});

// hover
export function createHoverStone() {
    hoverStone = document.createElement("div");
    hoverStone.className = "stone hover";
    boardElement.appendChild(hoverStone);
}

function placeStone(row, col) {
    if (board[row][col] !== 0) return;
    if (myRole !== currentTurn || myRole === 0) {
        return;
    }

    board[row][col] = currentTurn;
    renderStone(row, col);

    if (checkWin(row, col, currentTurn)) {
        setTimeout(() => {
            alert((currentTurn === 1 ? "흑" : "백") + " 승리!");
            location.reload();
        }, 100);
        return;
    }

    saveBoardToSession();

    currentTurn = currentTurn === 1 ? 2 : 1;
}

export function renderStone(row, col, color = board[row][col]) {
    const stone = document.createElement("div");
    stone.className = "stone " + (color === 1 ? "black" : "white");

    const left = gridStartX + col * cellSizeX;
    const top = gridStartY + row * cellSizeY;

    stone.style.left = `${left}px`;
    stone.style.top = `${top}px`;

    boardElement.appendChild(stone);
}

export function rerenderStones() {
    // 기존 돌 제거 (hover 제외)
    document.querySelectorAll(".stone:not(.hover)").forEach(el => el.remove());

    // 다시 렌더링
    for (let r = 0; r < boardSize; r++) {
        for (let c = 0; c < boardSize; c++) {
            if (board[r][c] !== 0) {
                renderStone(r, c, board[r][c]);
            }
        }
    }
}

export function checkWin(row, col, color) {
    const directions = [
        [0, 1],
        [1, 0],
        [1, 1],
        [1, -1]
    ];

    for (const [dy, dx] of directions) {
        let count = 1;

        count += countStones(row, col, dy, dx, color);
        count += countStones(row, col, -dy, -dx, color);

        if (count >= 5) {
            return true;
        }
    }

    return false;
}

export function countStones(row, col, dy, dx, color) {
    let count = 0;
    let r = row + dy;
    let c = col + dx;

    while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && board[r][c] === color) {
        count++;
        r += dy;
        c += dx;
    }

    return count;
}

export function saveBoardToSession() {
    sessionStorage.setItem('board', JSON.stringify(board));
    sessionStorage.setItem('turn', currentTurn);
}