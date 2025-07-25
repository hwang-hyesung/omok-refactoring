//오목 돌 배치
import {myRole} from "../matching/matching.js";

export const boardSize = 15;
export const board = Array.from({ length: boardSize }, () => Array(boardSize).fill(0));

export const boardElement = document.getElementById("board");
export const boardImage = document.getElementById("board-image");

export const borderRatio = 65 / 768;
export const offsetX = -4;
export const offsetY = -2;

let gridStartX, gridStartY, cellSizeX, cellSizeY;
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

export function getCellFromMouseEvent(e) {
    const rect = boardImage.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

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
//
// // 새로고침 하면 세션에 저장한 돌 정보 불러오기
// window.onload = () => {
//     calculateGridMetrics();
//
//     const savedBoard = sessionStorage.getItem('board');
//     const savedTurn = sessionStorage.getItem('turn');
//
//     if (savedBoard) {
//         const parsedBoard = JSON.parse(savedBoard);
//         for (let r = 0; r < boardSize; r++) {
//             for (let c = 0; c < boardSize; c++) {
//                 board[r][c] = parsedBoard[r][c];
//                 if (board[r][c] !== 0) {
//                     drawStone(r, c, board[r][c]);
//                 }
//             }
//         }
//     }
//
//     if (savedTurn) {
//         currentTurn = parseInt(savedTurn);
//     }
// };
//
// window.addEventListener('resize', () => {
//     calculateGridMetrics();
//     redrawStones();
//     if (hoverStone) {
//         hoverStone.style.display = 'none'; // 화면 크기 변경 시 hover 숨김
//     }
// });
//
// boardElement.addEventListener("mousemove", (e) => {
//     if (!hoverStone) createHoverStone();
//     if (myRole !== currentTurn) {
//         if (hoverStone) hoverStone.style.display = 'none';
//         return;
//     }
//     const rect = boardImage.getBoundingClientRect();
//     const x = e.clientX - rect.left;
//     const y = e.clientY - rect.top;
//
//     const cell = getCellFromMousePosition(x, y);
//
//     if (!cell || board[cell.row][cell.col] !== 0) {
//         hoverStone.style.display = 'none';
//         return;
//     }
//
//     hoverStone.style.display = 'block';
//
//     const left = gridStartX + cell.col * cellSizeX;
//     const top = gridStartY + cell.row * cellSizeY;
//
//     hoverStone.style.left = `${left}px`;
//     hoverStone.style.top = `${top}px`;
//     hoverStone.className = `stone hover ${currentTurn === 1 ? 'black' : 'white'}`;
// });
//
// boardElement.addEventListener("mouseleave", () => {
//     if (hoverStone) hoverStone.style.display = 'none';
// });
//
// //클릭시 돌 두기
// boardElement.addEventListener("click", (e) => {
//     const rect = boardImage.getBoundingClientRect();
//     const x = e.clientX - rect.left;
//     const y = e.clientY - rect.top;
//
//     const cell = getCellFromMousePosition(x, y);
//     if (!cell) return;
//
//     placeStone(cell.row, cell.col);
// });

// hover
export function createHoverStone() {
    hoverStone = document.createElement("div");
    hoverStone.className = "stone hover";
    boardElement.appendChild(hoverStone);
}

export function updateHoverStonePosition(row, col, color) {
    if (!hoverStone) createHoverStone();
    hoverStone.style.display = 'block';
    hoverStone.style.left = `${gridStartX + col * cellSizeX}px`;
    hoverStone.style.top = `${gridStartY + row * cellSizeY}px`;
    hoverStone.className = `stone hover ${color === 1 ? 'black' : 'white'}`;
}

export function drawStone(row, col, color) {
    const stone = document.createElement("div");
    stone.className = "stone " + (color === 1 ? "black" : "white");

    const left = gridStartX + col * cellSizeX;
    const top = gridStartY + row * cellSizeY;

    stone.style.left = `${left}px`;
    stone.style.top = `${top}px`;

    boardElement.appendChild(stone);
}

export function saveBoardToSession(currentTurn) {
    sessionStorage.setItem('board', JSON.stringify(board));
    sessionStorage.setItem('turn', currentTurn);
}

export function redrawStones() {
    // 기존 돌 제거 (hover 제외)
    document.querySelectorAll(".stone:not(.hover)").forEach(el => el.remove());

    // 다시 렌더링
    for (let r = 0; r < boardSize; r++) {
        for (let c = 0; c < boardSize; c++) {
            if (board[r][c] !== 0) {
                drawStone(r, c, board[r][c]);
            }
        }
    }
}

export function updateBoardData(newBoard) {
    for (let r = 0; r < boardSize; r++) {
        for (let c = 0; c < boardSize; c++) {
            board[r][c] = newBoard[r][c];
        }
    }
}

export function loadBoardFromSession() {
    const savedBoard = sessionStorage.getItem('board');
    const savedTurn = sessionStorage.getItem('turn');

    if (savedBoard) {
        updateBoardData(JSON.parse(savedBoard));
        redrawStones();
    }
    return savedTurn ? parseInt(savedTurn) : 1;
}

export function initBoardEvents(sendStone, currentTurn, myRole) {
    calculateGridMetrics();

    window.addEventListener('resize', () => {
        calculateGridMetrics();
        redrawStones();
        if (hoverStone) hoverStone.style.display = 'none';
    });

    boardElement.addEventListener('mousemove', (e) => {
        const cell = getCellFromMouseEvent(e);
        if (!cell || board[cell.row][cell.col] !== 0 || myRole !== currentTurn) {
            if (hoverStone) hoverStone.style.display = 'none';
            return;
        }
        if (myRole !== currentTurn) {
        if (hoverStone) hoverStone.style.display = 'none';
            return;
        }
        updateHoverStonePosition(cell.row, cell.col, currentTurn);
    });

    boardElement.addEventListener('mouseleave', (e) => {
        if (hoverStone) hoverStone.style.display = 'none';
    });

    boardElement.addEventListener('click', (e) => {
        const cell = getCellFromMouseEvent(e);
        if (cell) sendStone(cell.row, cell.col);
    });
}

